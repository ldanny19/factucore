package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;

public interface ComprobanteValidator {

	ComprobanteValidationResult validar(ComprobanteGeneracionRequest request);
}