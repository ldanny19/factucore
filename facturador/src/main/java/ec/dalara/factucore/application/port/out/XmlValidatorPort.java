package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

public interface XmlValidatorPort {

	void validar(String xml, DocumentDefinitionModel definition);
}