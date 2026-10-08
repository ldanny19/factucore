package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteWorkflowService implements ComprobanteWorkflowPort {

	private final WorkflowExecutionPort workflowExecutionPort;

	@Override
	public ComprobanteGeneracionResponse procesar(ComprobanteGeneracionRequest request) {
		return workflowExecutionPort.ejecutar(request);
	}

	@Override
	public void reprocesar(Long comprobanteId) {
		if (comprobanteId == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}

		workflowExecutionPort.reprocesar(comprobanteId);
	}
}
