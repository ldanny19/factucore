package ec.dalara.factucore.application.validation;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteGeneracionValidator implements ComprobanteValidator {

	private final MessageResolver messageResolver;
	private final ObjectMapper objectMapper;
	private final DocumentoDefinitionProvider documentoDefinitionProvider;
	private final JsonSchemaComprobanteValidator jsonSchemaComprobanteValidator;

	@Override
	public ComprobanteValidationResult validar(ComprobanteGeneracionRequest request) {
		ComprobanteValidationResult resultado = new ComprobanteValidationResult(messageResolver);

		if (request == null) {
			resultado.agregarError(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO, null);
			return resultado;
		}

		validarDocumentoOrigen(request, resultado);
		validarTipoDocumento(request, resultado);
		validarFechaInicio(request, resultado);
		validarDatos(request, resultado);
		validarDefinicionYDatos(request, resultado);

		return resultado;
	}

	private void validarDocumentoOrigen(ComprobanteGeneracionRequest request,
			ComprobanteValidationResult resultado) {
		if (request.getIdDocumentoOrigen() == null || request.getIdDocumentoOrigen() <= 0) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_REQUERIDO, "idDocumentoOrigen");
		}
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
		if (datos == null || !datos.isObject() || datos.isEmpty()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DATOS_REQUERIDOS, "datos");
		}
	}

	private void validarDefinicionYDatos(ComprobanteGeneracionRequest request,
			ComprobanteValidationResult resultado) {
		if (request.getIdTipoDocumento() == null || request.getVersionXsd() == null
				|| request.getVersionXsd().isBlank()) {
			return;
		}

		var definicionOptional = documentoDefinitionProvider.obtenerDefinicion(
				request.getIdTipoDocumento(), request.getVersionXsd());

		if (definicionOptional.isEmpty()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA, "idTipoDocumento",
					request.getIdTipoDocumento());
			return;
		}

		DocumentDefinitionModel definicion = definicionOptional.get();
		String esquemaJson = definicion.getVersion().getEsquemaJson();
		if (esquemaJson == null || esquemaJson.isBlank()) {
			resultado.agregarError(MessageCodes.COMPROBANTE_ESQUEMA_JSON_NO_CONFIGURADO, "esquemaJson",
					request.getVersionXsd());
			return;
		}

		jsonSchemaComprobanteValidator.validar(esquemaJson, request.getDatos(), resultado);
	}

	private Map<String, Object> convertirDatos(JsonNode datos) {
		if (datos == null || !datos.isObject()) {
			return Map.of();
		}
		return objectMapper.convertValue(datos, new TypeReference<Map<String, Object>>() {
		});
	}
}
