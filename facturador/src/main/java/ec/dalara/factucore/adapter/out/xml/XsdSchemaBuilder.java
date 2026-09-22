package ec.dalara.factucore.adapter.out.xml;

import java.io.StringReader;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;

@Component
public class XsdSchemaBuilder {

    public Schema construir(DocumentDefinitionModel definition) throws Exception {
        String xsd = construirXsd(definition);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newSchema(new StreamSource(new StringReader(xsd)));
    }

    private String construirXsd(DocumentDefinitionModel d) {
        String ns = d.getVersion().getNamespaceXml();
        StringBuilder x = new StringBuilder();
        x.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        x.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
        if (ns != null && !ns.isBlank()) {
            x.append(" targetNamespace=\"").append(escape(ns)).append("\"");
            x.append(" xmlns:tns=\"").append(escape(ns)).append("\"");
            x.append(" elementFormDefault=\"qualified\"");
        }
        x.append(">");
        ElementoXsdModel root = d.getElementos().stream()
                .filter(e -> e.getElementoPadreId() == null)
                .filter(e -> d.getVersion().getElementoRaiz() == null
                        || d.getVersion().getElementoRaiz().isBlank()
                        || Objects.equals(e.getNombre(), d.getVersion().getElementoRaiz()))
                .findFirst()
                .orElseThrow();
        appendElement(x, root, d, true);
        x.append("</xs:schema>");
        return x.toString();
    }

    private void appendElement(StringBuilder x, ElementoXsdModel e, DocumentDefinitionModel d, boolean root) {
        x.append("<xs:element name=\"").append(escape(e.getNombre())).append("\"");
        if (!root) {
            if (e.getMinOcurrencias() != null) {
                x.append(" minOccurs=\"").append(e.getMinOcurrencias()).append("\"");
            }
            if (e.getMaxOcurrencias() != null) {
                x.append(" maxOccurs=\"").append(e.getMaxOcurrencias()).append("\"");
            } else if (Boolean.TRUE.equals(e.getRepetible())) {
                x.append(" maxOccurs=\"unbounded\"");
            }
        }

        if (tieneHijos(e, d)) {
            x.append("><xs:complexType><xs:sequence>");
            d.getElementos().stream()
                    .filter(h -> Objects.equals(h.getElementoPadreId(), e.getId()))
                    .sorted(java.util.Comparator.comparing(ElementoXsdModel::getOrden,
                            java.util.Comparator.nullsLast(Integer::compareTo)))
                    .forEach(h -> appendElement(x, h, d, false));
            x.append("</xs:sequence>");
            appendAttributes(x, e, d);
            x.append("</xs:complexType></xs:element>");
        } else {
            appendSimpleType(x, e, d);
            x.append("</xs:element>");
        }
    }

    private void appendSimpleType(StringBuilder x, ElementoXsdModel e, DocumentDefinitionModel d) {
        String base = tipoBase(e.getTipoDato());
        boolean restricciones = e.getLongitudMinima() != null || e.getLongitudMaxima() != null
                || e.getDigitosTotales() != null || e.getDecimales() != null
                || e.getValorMinimo() != null || e.getValorMaximo() != null
                || (e.getPatron() != null && !e.getPatron().isBlank())
                || d.getEnumeraciones().stream().anyMatch(v -> Objects.equals(v.getElementoXsdId(), e.getId()));

        if (!restricciones) {
            x.append(" type=\"").append(base).append("\"");
            return;
        }

        x.append("><xs:simpleType><xs:restriction base=\"").append(base).append("\">");
        if (e.getLongitudMinima() != null) x.append("<xs:minLength value=\"").append(e.getLongitudMinima()).append("\"/>");
        if (e.getLongitudMaxima() != null) x.append("<xs:maxLength value=\"").append(e.getLongitudMaxima()).append("\"/>");
        if (e.getDigitosTotales() != null) x.append("<xs:totalDigits value=\"").append(e.getDigitosTotales()).append("\"/>");
        if (e.getDecimales() != null) x.append("<xs:fractionDigits value=\"").append(e.getDecimales()).append("\"/>");
        if (e.getValorMinimo() != null) x.append("<xs:minInclusive value=\"").append(escape(e.getValorMinimo().toPlainString())).append("\"/>");
        if (e.getValorMaximo() != null) x.append("<xs:maxInclusive value=\"").append(escape(e.getValorMaximo().toPlainString())).append("\"/>");
        if (e.getPatron() != null && !e.getPatron().isBlank()) x.append("<xs:pattern value=\"").append(escape(e.getPatron())).append("\"/>");
        d.getEnumeraciones().stream()
                .filter(v -> Objects.equals(v.getElementoXsdId(), e.getId()))
                .forEach(v -> x.append("<xs:enumeration value=\"").append(escape(v.getValor())).append("\"/>"));
        x.append("</xs:restriction></xs:simpleType>");
    }

    private void appendAttributes(StringBuilder x, ElementoXsdModel e, DocumentDefinitionModel d) {
        d.getAtributos().stream()
                .filter(a -> Objects.equals(a.getElementoXsdId(), e.getId()))
                .forEach(a -> {
                    String tipo = tipoBase(a.getTipoDato());
                    x.append("<xs:attribute name=\"").append(escape(a.getNombre())).append("\"");
                    if (Boolean.TRUE.equals(a.getObligatorio())) x.append(" use=\"required\"");
                    if (a.getValorPredeterminado() != null) {
                        x.append(" default=\"").append(escape(a.getValorPredeterminado())).append("\"");
                    }

                    if (a.getPatron() == null || a.getPatron().isBlank()) {
                        x.append(" type=\"").append(tipo).append("\"/>");
                    } else {
                        x.append("><xs:simpleType><xs:restriction base=\"").append(tipo)
                                .append("\"><xs:pattern value=\"").append(escape(a.getPatron()))
                                .append("\"/></xs:restriction></xs:simpleType></xs:attribute>");
                    }
                });
    }

    private boolean tieneHijos(ElementoXsdModel e, DocumentDefinitionModel d) {
        return d.getElementos().stream().anyMatch(h -> Objects.equals(h.getElementoPadreId(), e.getId()));
    }

    private String tipoBase(String tipo) {
        if (tipo == null || tipo.isBlank()) return "xs:string";
        String t = tipo.contains(":") ? tipo.substring(tipo.indexOf(':') + 1) : tipo;
        return switch (t.toLowerCase()) {
            case "integer", "int", "long" -> "xs:long";
            case "decimal" -> "xs:decimal";
            case "double" -> "xs:double";
            case "float" -> "xs:float";
            case "boolean" -> "xs:boolean";
            case "date" -> "xs:date";
            case "datetime" -> "xs:dateTime";
            default -> "xs:string";
        };
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;")
                .replace("<", "&lt;").replace(">", "&gt;");
    }
}
