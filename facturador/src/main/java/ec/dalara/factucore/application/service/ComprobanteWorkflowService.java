package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.port.in.ComprobanteWorkflowPort;
import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.application.validation.ComprobanteGeneracionValidator;
import ec.dalara.factucore.application.validation.ComprobanteValidationResult;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteWorkflowService implements ComprobanteWorkflowPort {

	private final ComprobanteGeneracionValidator validator;
	private final WorkflowExecutionPort workflowExecutionPort;

	@Override
	public ComprobanteGeneracionResponse procesar(ComprobanteGeneracionRequest request) {
		validar(request);

		return workflowExecutionPort.ejecutar(request);
	}

	@Override
	public void reprocesar(Long comprobanteId) {

		if (comprobanteId == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}

		workflowExecutionPort.reprocesar(comprobanteId);
	}

	private void validar(ComprobanteGeneracionRequest request) {
		ComprobanteValidationResult resultado = validator.validar(request);

		if (!resultado.esValido()) {

			var primerError = resultado.getErrores().get(0);

			throw new ApplicationException(primerError.getCodigo());
		}
	}
}