package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;

public interface WorkflowExecutionPort {

	ComprobanteGeneracionResponse ejecutar(ComprobanteGeneracionRequest request);

	void reprocesar(Long comprobanteId);
}