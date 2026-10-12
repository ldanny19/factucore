package ec.dalara.factucore.application.workflow;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.service.ComprobanteHashService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.application.validation.ComprobanteGeneracionValidator;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionComprobanteWorkflowStep {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final ComprobanteGeneracionValidator validator;
	private final ComprobanteService comprobanteService;
	private final ComprobanteHashService hashService;
	private final MessageResolver messageResolver;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION;
	}

	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
		}

		var solicitud = contexto.getSolicitud();
		var validacion = validator.validar(solicitud);
		contexto.registrarErroresValidacion(validacion.getErrores());
		if (!validacion.esValido()) {
			var error = validacion.getErrores().isEmpty() ? null : validacion.getErrores().get(0);
			return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "RECHAZADA",
					error == null ? MessageCodes.COMPROBANTE_REQUEST_REQUERIDO : error.getCodigo(),
					error == null ? null : error.getMensaje());
		}

		var hashes = hashService.calcular(solicitud);
		contexto.setHashIdentidad(hashes.identidad());
		contexto.setHashContenido(hashes.contenido());

		var porOrigen = comprobanteService.obtenerPorEmpresaEIdDocumentoOrigen(
				solicitud.getIdEmpresa(), solicitud.getIdDocumentoOrigen());
		if (porOrigen.isPresent()) {
			var anterior = porOrigen.get();
			if (anterior.getHashIdentidad() == null || anterior.getHashContenido() == null
					|| !anterior.getHashIdentidad().equals(hashes.identidad())) {
				return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CONFLICTO",
						MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO,
						messageResolver.resolver(MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, solicitud.getIdDocumentoOrigen()));
			}

			if (anterior.getHashContenido().equals(hashes.contenido())) {
				contexto.asignarComprobante(anterior);
				contexto.setSecuencial(anterior.getSecuencial());
				contexto.setClaveAcceso(anterior.getClaveAcceso());
				contexto.setEstadoSri(anterior.getEstadoSri());
				contexto.setNumeroAutorizacion(anterior.getNumeroAutorizacion());
				contexto.marcarIdempotente();
				return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
			}

			if (!esNoAutorizado(anterior.getEstadoSri())) {
				return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CORRECCION_NO_PERMITIDA",
						MessageCodes.COMPROBANTE_CORRECCION_NO_PERMITIDA,
						messageResolver.resolver(MessageCodes.COMPROBANTE_CORRECCION_NO_PERMITIDA, solicitud.getIdDocumentoOrigen()));
			}

			contexto.setComprobanteReemplazado(anterior);
			contexto.setSecuencial(anterior.getSecuencial());
			contexto.setClaveAcceso(anterior.getClaveAcceso());
			return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "CORRECCION");
		}

		var porTransaccion = comprobanteService.obtenerPorEmpresaEIdTransaccion(
				solicitud.getIdEmpresa(), solicitud.getIdTransaccion());
		if (porTransaccion.isPresent()) {
			var existente = porTransaccion.get();
			if (java.util.Objects.equals(solicitud.getIdDocumentoOrigen(), existente.getIdDocumentoOrigen())
					&& hashes.identidad().equals(existente.getHashIdentidad())
					&& hashes.contenido().equals(existente.getHashContenido())) {
				contexto.asignarComprobante(existente);
				contexto.setSecuencial(existente.getSecuencial());
				contexto.setClaveAcceso(existente.getClaveAcceso());
				contexto.setEstadoSri(existente.getEstadoSri());
				contexto.setNumeroAutorizacion(existente.getNumeroAutorizacion());
				contexto.marcarIdempotente();
				return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
			}
			return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CONFLICTO",
					MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO,
					messageResolver.resolver(MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, solicitud.getIdDocumentoOrigen()));
		}

		return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "VALIDADA");
	}

	private boolean esNoAutorizado(String estadoSri) {
		if (estadoSri == null) {
			return false;
		}
		String normalizado = Normalizer.normalize(estadoSri, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replaceAll("[^A-Za-z]", "")
				.toUpperCase(Locale.ROOT);
		return "NOAUTORIZADO".equals(normalizado);
	}
}
