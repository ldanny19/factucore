package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;

import java.util.Map;

public interface XmlGeneratorPort {

    String generar(
            DocumentDefinitionModel definition,
            Map<String, Object> datos
    );
}