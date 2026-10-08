package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

@Component
public class WorkflowContextFactory {

	public ContextoWorkflow crear(ComprobanteGeneracionRequest request) {
		return ContextoWorkflow.nuevo(request);
	}
}
