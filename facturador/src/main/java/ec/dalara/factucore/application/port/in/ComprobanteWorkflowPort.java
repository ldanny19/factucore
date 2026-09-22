package ec.dalara.factucore.application.port.in;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;

public interface ComprobanteWorkflowPort {

	ComprobanteGeneracionResponse procesar(ComprobanteGeneracionRequest request);

	void reprocesar(Long comprobanteId);
}