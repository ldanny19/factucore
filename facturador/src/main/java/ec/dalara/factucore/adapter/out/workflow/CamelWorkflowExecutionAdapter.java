package ec.dalara.factucore.adapter.out.workflow;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CamelWorkflowExecutionAdapter implements WorkflowExecutionPort {

	private static final String ENDPOINT_PROCESAR = "direct:facturador.workflow.procesar";

	private static final String ENDPOINT_REPROCESAR = "direct:facturador.workflow.reprocesar";

	private final ProducerTemplate producerTemplate;

	@Override
	public ComprobanteGeneracionResponse ejecutar(ComprobanteGeneracionRequest request) {
		return producerTemplate.requestBody(ENDPOINT_PROCESAR, request, ComprobanteGeneracionResponse.class);
	}

	@Override
	public void reprocesar(Long comprobanteId) {
		producerTemplate.sendBody(ENDPOINT_REPROCESAR, comprobanteId);
	}
}