package ec.dalara.factucore.application.validation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteGeneracionValidator implements ComprobanteValidator {

	private final MessageResolver messageResolver;
	private final ObjectMapper objectMapper;
	private final DocumentoDefinitionProvider documentoDefinitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final DocumentDefinitionDataValidator documentDefinitionDataValidator;

	@Override
	public ComprobanteValidationResult validar(ComprobanteGeneracionRequest request) {
		ComprobanteValidationResult resultado = new ComprobanteValidationResult(messageResolver);

		if (request == null) {
			resultado.agregarError(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO, null);

			return resultado;
		}

		validarTipoDocumento(request, resultado);
		validarFechaInicio(request, resultado);
		validarDatos(request, resultado);

		if (!resultado.esValido()) {
			return resultado;
		}

		validarDefinicionYDatos(request, resultado);

		return resultado;
	}

	private void validarTipoDocumento(ComprobanteGeneracionRequest request, ComprobanteValidationResult resultado) {
		if (request.getIdTipoDocumento() == null) {
			resultado.agregarError(MessageCodes.COMPROBANTE_TIPO_DOCUMENTO_REQUERIDO, "idTipoDocumento");
		}
	}

	private void validarFechaInicio(ComprobanteGeneracionRequest request, ComprobanteValidationResult resultado) {
		if (request.getFechaInicio() == null) {

			resultado.agregarError(MessageCodes.COMPROBANTE_FECHA_INICIO_REQUERIDA, "fechaInicio");
		}
	}

	private void validarDatos(ComprobanteGeneracionRequest request, ComprobanteValidationResult resultado) {
		JsonNode datos = request.getDatos();

		if (datos == null || datos.isNull() || !datos.isObject() || datos.isEmpty()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DATOS_REQUERIDOS, "datos");
		}
	}


	private void validarDefinicionYDatos(ComprobanteGeneracionRequest request, ComprobanteValidationResult resultado) {
		OffsetDateTime fechaInicio = request.getFechaInicio();

		if (fechaInicio == null) {
			return;
		}

		LocalDateTime fechaEmision = convertirFecha(fechaInicio);

		var documentoOptional = documentoXsdService.obtenerPorId(request.getIdTipoDocumento());
		if (documentoOptional.isEmpty()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA, "idTipoDocumento", request.getIdTipoDocumento());
			return;
		}

		var codigoDocumento = documentoOptional.get().getCodigo();
		var definicionOptional = documentoDefinitionProvider.obtenerDefinicionVigente(codigoDocumento, fechaEmision);

		if (definicionOptional.isEmpty()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA, "idTipoDocumento", request.getIdTipoDocumento());
			return;
		}

		DocumentDefinitionModel definicion = definicionOptional.get();

		Map<String, Object> datos = convertirDatos(request.getDatos());

		documentDefinitionDataValidator.validar(definicion, datos, resultado);
	}

	private Map<String, Object> convertirDatos(JsonNode datos) {
		if (datos == null || !datos.isObject()) {
			return Map.of();
		}

		return objectMapper.convertValue(datos, new TypeReference<Map<String, Object>>() {
		});
	}

	private LocalDateTime convertirFecha(OffsetDateTime fecha) {
		return fecha.toLocalDateTime();
	}
}