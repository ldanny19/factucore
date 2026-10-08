package ec.dalara.factucore.application.workflow;

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

	private final ComprobanteWorkflowResponseFactory responseFactory;

	public String ejecutar(ContextoWorkflow contexto) {
		if (contexto != null) {
			contexto.marcarErrorNotificacion();
			contexto.setExitosoFinal(true);
			contexto.setRespuesta(responseFactory.crear(contexto));
		}
		return "OK";
	}
}
