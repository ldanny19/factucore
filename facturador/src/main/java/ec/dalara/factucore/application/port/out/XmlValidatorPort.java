package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.application.validation.ComprobanteValidationResult;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

public interface XmlValidatorPort {

	ComprobanteValidationResult validar(String xml, DocumentDefinitionModel definition);
}