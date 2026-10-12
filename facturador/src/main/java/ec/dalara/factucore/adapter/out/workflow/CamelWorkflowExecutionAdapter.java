package ec.dalara.factucore.adapter.out.workflow;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que conecta la aplicación con el workflow ejecutado por
 * Apache Camel.
 *
 * <p>La aplicación conoce únicamente {@code WorkflowExecutionPort}. Este
 * adaptador traduce esa llamada a los endpoints {@code direct:*} definidos en
 * el XML externo. No contiene reglas de negocio ni conoce las transiciones
 * entre etapas.</p>
 */
@Component
@RequiredArgsConstructor
public class CamelWorkflowExecutionAdapter implements WorkflowExecutionPort {

	private static final String ENDPOINT_PROCESAR = "direct:facturador.workflow.procesar";

	private final ProducerTemplate producerTemplate;

	@Override
	public ComprobanteGeneracionResponse ejecutar(ComprobanteGeneracionRequest request) {
		return producerTemplate.requestBody(ENDPOINT_PROCESAR, request, ComprobanteGeneracionResponse.class);
	}

	@Override
	public ComprobanteGeneracionResponse reprocesar(ContextoWorkflow contexto) {
		return producerTemplate.requestBodyAndHeader(ENDPOINT_PROCESAR, contexto, "workflow.reprocesar", true,
				ComprobanteGeneracionResponse.class);
	}
}
