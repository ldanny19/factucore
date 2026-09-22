package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;

public interface WorkflowStep {

	EtapaWorkflow etapa();

	ResultadoEtapa ejecutar(ContextoWorkflow contexto);
}