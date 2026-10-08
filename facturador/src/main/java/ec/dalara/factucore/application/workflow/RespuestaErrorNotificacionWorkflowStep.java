package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;


import ec.dalara.factucore.application.MessageResolver;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;

import ec.dalara.factucore.domain.workflow.EtapaWorkflow;


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

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final MessageResolver messageResolver;
	private final ComprobanteWorkflowResponseFactory responseFactory;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.RESPUESTA_ERROR_NOTIFICACION;
	}

	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto != null) {
			contexto.marcarErrorNotificacion();
			contexto.setExitosoFinal(true);
			contexto.setRespuesta(responseFactory.crear(contexto));
		}
		return ResultadoEtapa.exitosa(etapa(), "COMPLETADA");
	}}
