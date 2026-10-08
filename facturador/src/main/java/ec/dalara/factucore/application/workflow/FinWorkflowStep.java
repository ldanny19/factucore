package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.shared.MessageCodes;

import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

/**
 * Nodo terminal único del workflow de facturación.
 *
 * <p>FIN no contiene reglas de negocio ni decide transiciones. Su operación
 * principal devuelve {@code FIN}, que representa exclusivamente el cierre del
 * grafo. La respuesta pública ya fue preparada por el nodo de respuesta y este
 * nodo únicamente la entrega al final de la ruta Camel.</p>
 */
@Component
@RequiredArgsConstructor
public class FinWorkflowStep {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final MessageResolver messageResolver;
	private final ComprobanteWorkflowResponseFactory responseFactory;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.FIN;
	}

	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		return ResultadoEtapa.exitosa(etapa(), "FIN");
	}
	public ComprobanteGeneracionResponse respuesta(ContextoWorkflow contexto) {
		if (contexto == null) {
			return null;
		}
		if (contexto.getRespuesta() == null) {
			contexto.setRespuesta(responseFactory.crear(contexto));
		}
		return contexto.getRespuesta();
	}
}
