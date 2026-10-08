package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;

import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionWorkflowStep   {

	private static final Logger log = LoggerFactory.getLogger(NotificacionWorkflowStep.class);

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;
	private final NotificacionPort notificacionPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.NOTIFICACION;
	}

	/** Ejecuta la notificación y devuelve a Camel solo OK o ERROR. */
	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		if (contexto != null) {
			contexto.iniciarEtapa(etapa());
		}
		try {
			ResultadoEtapa resultado = ejecutarResultado(contexto);
			String salida = workflowResultadoService.registrar(contexto, resultado);
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			String codigo = exception instanceof ApplicationException applicationException
					? applicationException.getCodigo()
					: MessageCodes.WORKFLOW_ETAPA_ERROR;
			Object[] parametros = exception instanceof ApplicationException applicationException
					? applicationException.getParametros()
					: new Object[] { etapa().name() };
			String mensaje = messageResolver.resolver(codigo, parametros);
			ResultadoEtapa resultadoError = ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), codigo, mensaje);
			if (contexto != null) {
				workflowResultadoService.registrar(contexto, resultadoError);;
			}
			log.error("ID_TRANSACCION={} - Error Etapa {} - codigo={} - mensaje={}", idTransaccion, etapa(), codigo, mensaje,
					exception);
			String salida = resultadoError.salida();
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		}
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getComprobante() == null) {
			throw new ApplicationException(MessageCodes.NOTIFICACION_COMPROBANTE_REQUERIDO);
		}

		var comprobante = contexto.getComprobante();

		if (!EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())) {
			return ResultadoEtapa.exitosa(etapa(), "NOTIFICACION_ERROR");
		}

		try {
			notificacionPort.publicar(contexto);
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.RIDE_GENERADO.name(),
					Map.of("notificacionPublicada", true));
		} catch (RuntimeException exception) {
			return ResultadoEtapa.exitosa(etapa(), "NOTIFICACION_ERROR");
		}
	}
}
