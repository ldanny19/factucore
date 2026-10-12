package ec.dalara.factucore.application.validation;

import java.util.Map;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

public interface DocumentDefinitionValidator {

	void validar(DocumentDefinitionModel definition, Map<String, Object> datos, ComprobanteValidationResult resultado);
}