package ec.dalara.factucore.domain.documentoxsd.importacion;

import java.util.List;

public record XsdDefinitionSource(
        String namespaceXml, String elementoRaiz,
        List<XsdElementSource> elementos,
        List<XsdAttributeSource> atributos,
        List<XsdEnumerationSource> enumeraciones) {
    public XsdDefinitionSource {
        elementos = List.copyOf(elementos);
        atributos = List.copyOf(atributos);
        enumeraciones = List.copyOf(enumeraciones);
    }
}
