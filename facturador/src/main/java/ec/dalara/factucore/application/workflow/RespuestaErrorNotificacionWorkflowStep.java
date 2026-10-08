package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;

import ec.dalara.factucore.domain.workflow.EtapaWorkflow;

import ec.dalara.factucore.domain.workflow.EstadoProceso;

import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

/**
 * Prepara la respuesta cuando falla exclusivamente la entrega de correo.
 *
 * <p>El fallo de notificación no invalida la facturación. Por eso esta etapa
 * marca el proceso como exitoso, agrega la condición de notificación al
 * contexto y prepara una respuesta que contiene el mensaje informativo
 * correspondiente. Después devuelve {@code OK} para continuar a {@code FIN}.</p>
 */
@Component
@RequiredArgsConstructor
public class RespuestaErrorNotificacionWorkflowStep {

	private static final Logger log = LoggerFactory.getLogger(RespuestaErrorNotificacionWorkflowStep.class);

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;
	private final ComprobanteWorkflowResponseFactory responseFactory;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.RESPUESTA_ERROR_NOTIFICACION;
	}

	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		if (contexto != null) {
			contexto.iniciarEtapa(etapa());
		}
		try {
			if (contexto != null) {
				contexto.marcarErrorNotificacion();
				contexto.setExitosoFinal(true);
				contexto.setRespuesta(responseFactory.crear(contexto));
			}
			ResultadoEtapa resultado = ResultadoEtapa.exitosa(etapa(), "COMPLETADA");
			if (contexto != null) {
				contexto.registrarResultado(resultado);
			}
			String salida = resultado.salida();
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
				workflowResultadoService.registrar(contexto, resultadoError);
			}
			log.error("ID_TRANSACCION={} - Error Etapa {} - codigo={} - mensaje={}", idTransaccion, etapa(), codigo, mensaje,
					exception);
			String salida = resultadoError.salida();
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		}
	}}
