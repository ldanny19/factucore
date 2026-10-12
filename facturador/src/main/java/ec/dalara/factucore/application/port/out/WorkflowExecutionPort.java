package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

public interface WorkflowExecutionPort {

	ComprobanteGeneracionResponse ejecutar(ComprobanteGeneracionRequest request);

	ComprobanteGeneracionResponse reprocesar(ContextoWorkflow contexto);
}
