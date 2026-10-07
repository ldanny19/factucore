package ec.dalara.factucore.application.port.out;

import java.util.Map;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

public interface XmlGeneratorPort {

    String generar(
            DocumentDefinitionModel definition,
            Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore,
            Map<String, Object> contextoGenerado);

    default String generar(DocumentDefinitionModel definition, Map<String, Object> contextoJson) {
        return generar(definition, contextoJson, Map.of(), Map.of());
    }
}
