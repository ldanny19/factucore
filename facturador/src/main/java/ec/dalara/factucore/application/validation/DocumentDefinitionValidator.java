package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

import java.util.Map;

public interface DocumentDefinitionValidator {

    void validar(
            DocumentDefinitionModel definition,
            Map<String, Object> datos,
            ComprobanteValidationResult resultado
    );
}