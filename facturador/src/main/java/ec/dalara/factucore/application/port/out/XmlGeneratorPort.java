package ec.dalara.factucore.application.port.out;

import java.util.Map;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

public interface XmlGeneratorPort {

	String generar(DocumentDefinitionModel definition, Map<String, Object> datos, Map<String, Object> contexto);

	default String generar(DocumentDefinitionModel definition, Map<String, Object> datos) {
		return generar(definition, datos, Map.of());
	}
}
