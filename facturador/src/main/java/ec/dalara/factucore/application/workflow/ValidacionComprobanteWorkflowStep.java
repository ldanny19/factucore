package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.validation.ComprobanteGeneracionValidator;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionComprobanteWorkflowStep  {
	private static final Logger log = LoggerFactory.getLogger(ValidacionComprobanteWorkflowStep.class);
	private final ComprobanteGeneracionValidator validator;
	private final ComprobanteService comprobanteService;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		try {
			ResultadoEtapa resultado = ejecutarResultado(contexto);
			String salida = contexto.registrarYObtenerSalida(resultado);
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado=ERROR", idTransaccion, etapa());
			throw exception;
		}
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
		}
		var resultado = validator.validar(contexto.getSolicitud());
		contexto.registrarErroresValidacion(resultado.getErrores());
		if (!resultado.esValido()) {
			resultado.getErrores().forEach(error -> log.error("Workflow etapa={} codigo={} campo={} mensaje={}", etapa(), error.getCodigo(), error.getCampo(), error.getMensaje()));
			var error = resultado.getErrores().isEmpty() ? null : resultado.getErrores().get(0);
			return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "RECHAZADA",
				error == null ? MessageCodes.COMPROBANTE_REQUEST_REQUERIDO : error.getCodigo(),
				error == null ? null : error.getMensaje());
		}

		var comprobanteExistente = comprobanteService.obtenerPorEmpresaEIdTransaccion(
				contexto.getSolicitud().getIdEmpresa(),
				contexto.getSolicitud().getIdTransaccion());

		if (comprobanteExistente.isPresent()) {
			var comprobante = comprobanteExistente.get();
			contexto.asignarComprobante(comprobante);
			contexto.setClaveAcceso(comprobante.getClaveAcceso());
			contexto.setSecuencial(comprobante.getSecuencial());
			contexto.marcarIdempotente();
			return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "IDEMPOTENTE");
		}

		return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "VALIDADA");
	}
}
