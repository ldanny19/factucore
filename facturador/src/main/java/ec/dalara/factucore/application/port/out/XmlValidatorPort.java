package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.application.validation.ComprobanteValidationResult;

public interface XmlValidatorPort {

	ComprobanteValidationResult validar(String xml, DocumentDefinitionModel definition);
}