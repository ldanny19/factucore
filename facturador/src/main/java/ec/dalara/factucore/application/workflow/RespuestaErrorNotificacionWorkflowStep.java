package ec.dalara.factucore.application.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
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

	private final ComprobanteWorkflowResponseFactory responseFactory;

	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa RESPUESTA_ERROR_NOTIFICACION", idTransaccion);
		try {
			if (contexto != null) {
				contexto.marcarErrorNotificacion();
				contexto.setExitosoFinal(true);
				contexto.setRespuesta(responseFactory.crear(contexto));
			}
			log.debug("ID_TRANSACCION={} - Fin Etapa RESPUESTA_ERROR_NOTIFICACION - Resultado={}", idTransaccion, "OK");
			return "OK";
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa RESPUESTA_ERROR_NOTIFICACION - Resultado=ERROR", idTransaccion);
			throw exception;
		}
	}}
