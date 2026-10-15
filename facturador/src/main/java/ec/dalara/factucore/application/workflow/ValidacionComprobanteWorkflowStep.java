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

		var porOrigen = comprobanteService.obtenerPorEmpresaEIdDocumentoOrigen(solicitud.getIdEmpresa(),
				solicitud.getIdDocumentoOrigen());
		if (porOrigen.isPresent()) {
			var anterior = porOrigen.get();

			// El identificador de origen es único por transacción y no puede reutilizarse
			// para una identidad documental distinta.
			if (anterior.getHashIdentidad() == null || anterior.getHashContenido() == null
					|| !anterior.getHashIdentidad().equals(hashes.identidad())) {
				return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CONFLICTO",
						MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, messageResolver.resolver(
								MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, solicitud.getIdDocumentoOrigen()));
			}

			if (anterior.getHashContenido().equals(hashes.contenido())) {
				asignarComprobanteExistente(contexto, anterior);
				return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
			}

			// No se reemplaza un comprobante autorizado ni se reenvía uno cuya
			// recepción/autorización continúe pendiente en el SRI.
			if (!esElegibleParaCorreccion(anterior)) {
				return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CORRECCION_NO_PERMITIDA",
						MessageCodes.COMPROBANTE_CORRECCION_NO_PERMITIDA, messageResolver.resolver(
								MessageCodes.COMPROBANTE_CORRECCION_NO_PERMITIDA, solicitud.getIdDocumentoOrigen()));
			}

			contexto.setComprobanteReemplazado(anterior);
			contexto.setSecuencial(anterior.getSecuencial());
			contexto.setClaveAcceso(anterior.getClaveAcceso());
			return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "CORRECCION");
		}

		var porTransaccion = comprobanteService.obtenerPorEmpresaEIdTransaccion(solicitud.getIdEmpresa(),
				solicitud.getIdTransaccion());
		if (porTransaccion.isPresent()) {
			var existente = porTransaccion.get();
			if (java.util.Objects.equals(solicitud.getIdDocumentoOrigen(), existente.getIdDocumentoOrigen())
					&& hashes.identidad().equals(existente.getHashIdentidad())
					&& hashes.contenido().equals(existente.getHashContenido())) {
				asignarComprobanteExistente(contexto, existente);
				return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
			}
			return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "CONFLICTO",
					MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, messageResolver.resolver(
							MessageCodes.COMPROBANTE_DOCUMENTO_ORIGEN_CONFLICTO, solicitud.getIdDocumentoOrigen()));
		}

		// Si cambia el identificador de origen o de transacción, la combinación de
		// hashes sigue permitiendo reconocer el mismo comprobante ya procesado.
		var porHashes = comprobanteService.obtenerPorEmpresaEHashes(solicitud.getIdEmpresa(), hashes.identidad(),
				hashes.contenido());
		if (porHashes.isPresent()) {
			asignarComprobanteExistente(contexto, porHashes.get());
			return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
		}

		return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "VALIDADA");
	}

	private void asignarComprobanteExistente(ContextoWorkflow contexto,
			ec.dalara.factucore.infrastructure.persistence.entity.Comprobante comprobante) {
		contexto.asignarComprobante(comprobante);
		contexto.setSecuencial(comprobante.getSecuencial());
		contexto.setClaveAcceso(comprobante.getClaveAcceso());
		contexto.setEstadoSri(comprobante.getEstadoSri());
		contexto.setNumeroAutorizacion(comprobante.getNumeroAutorizacion());
		contexto.marcarIdempotente();
	}

	private boolean esElegibleParaCorreccion(
			ec.dalara.factucore.infrastructure.persistence.entity.Comprobante comprobante) {
		if (comprobante == null || comprobante.getNumeroAutorizacion() != null
				|| comprobante.getFechaAutorizacion() != null) {
			return false;
		}

		// Solo los estados explícitos de rechazo permiten corregir y reenviar.
		// Los estados pendientes/desconocidos no pasan esta lista de autorización.
		String estadoNormalizado = normalizarEstado(comprobante.getEstadoSri());
		return "NOAUTORIZADO".equals(estadoNormalizado) || "RECHAZADO".equals(estadoNormalizado);
	}

	private String normalizarEstado(String estadoSri) {
		if (estadoSri == null) {
			return "";
		}
		return Normalizer.normalize(estadoSri, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
				.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
	}
}
