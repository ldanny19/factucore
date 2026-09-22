package ec.dalara.factucore.infrastructure.documentoxsd;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ec.dalara.factucore.application.port.out.XsdParserPort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdAttributeSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdElementSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdEnumerationSource;
import ec.dalara.factucore.infrastructure.InfrastructureException;

@Component
public class XsdParserAdapter implements XsdParserPort {

    private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    @Override
    public XsdDefinitionSource parse(InputStream inputStream, String systemId) {
        if (inputStream == null) {
            throw new InfrastructureException("FACTUCORE.XSD.ARCHIVO.REQUERIDO");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(inputStream);
            Element schema = document.getDocumentElement();
            if (!XSD_NS.equals(schema.getNamespaceURI()) || !"schema".equals(schema.getLocalName())) {
                throw new InfrastructureException("FACTUCORE.XSD.ESQUEMA.INVALIDO");
            }

            Map<String, Element> complexTypes = indexChildren(schema, "complexType");
            Map<String, Element> simpleTypes = indexChildren(schema, "simpleType");
            Element root = firstElement(schema, "element");
            if (root == null) {
                throw new InfrastructureException("FACTUCORE.XSD.ELEMENTO_RAIZ.NO_DEFINIDO");
            }

            List<XsdElementSource> elementos = new ArrayList<>();
            List<XsdAttributeSource> atributos = new ArrayList<>();
            List<XsdEnumerationSource> enumeraciones = new ArrayList<>();

            parseElement(root, "", 1, complexTypes, simpleTypes, elementos, atributos, enumeraciones);

            return new XsdDefinitionSource(
                    attr(schema, "targetNamespace"), attr(root, "name"),
                    elementos, atributos, enumeraciones);
        } catch (InfrastructureException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InfrastructureException("FACTUCORE.XSD.PARSE.ERROR", exception);
        }
    }

    private void parseElement(Element element, String parentPath, int order,
            Map<String, Element> complexTypes, Map<String, Element> simpleTypes,
            List<XsdElementSource> elementos, List<XsdAttributeSource> atributos,
            List<XsdEnumerationSource> enumeraciones) {

        String name = attr(element, "name");
        if (name == null || name.isBlank()) {
            throw new InfrastructureException("FACTUCORE.XSD.ELEMENTO.NOMBRE.REQUERIDO");
        }

        String path = parentPath.isBlank() ? name : parentPath + "." + name;
        Integer min = integerAttr(element, "minOccurs", 1);
        Integer max = maxOccurs(element);
        String typeName = localTypeName(attr(element, "type"));

        Element complexType = firstChild(element, "complexType");
        Element simpleType = firstChild(element, "simpleType");
        if (complexType == null) complexType = findType(complexTypes, typeName);
        if (simpleType == null) simpleType = findType(simpleTypes, typeName);

        Restriction restriction = resolveRestriction(simpleType, simpleTypes);

        elementos.add(new XsdElementSource(path, name, typeName, order, min, max,
                restriction.minLength, restriction.maxLength, restriction.totalDigits,
                restriction.fractionDigits, restriction.minInclusive, restriction.maxInclusive,
                restriction.pattern));

        if (simpleType != null) parseEnumerations(simpleType, path, enumeraciones);
        if (complexType != null) parseComplexType(complexType, path, complexTypes, simpleTypes,
                elementos, atributos, enumeraciones);
    }

    private void parseComplexType(Element complexType, String parentPath,
            Map<String, Element> complexTypes, Map<String, Element> simpleTypes,
            List<XsdElementSource> elementos, List<XsdAttributeSource> atributos,
            List<XsdEnumerationSource> enumeraciones) {

        Element sequence = firstChild(complexType, "sequence");
        if (sequence != null) {
            parseParticle(sequence, parentPath, complexTypes, simpleTypes, elementos, atributos, enumeraciones);
        }
        if (firstChild(complexType, "choice") != null) {
            throw new InfrastructureException("FACTUCORE.XSD.CHOICE.NO_SOPORTADO", parentPath);
        }
        if (firstChild(complexType, "all") != null) {
            throw new InfrastructureException("FACTUCORE.XSD.ALL.NO_SOPORTADO", parentPath);
        }
        for (Element attribute : children(complexType, "attribute")) {
            parseAttribute(attribute, parentPath, simpleTypes, atributos);
        }
    }

    private void parseParticle(Element particle, String parentPath,
            Map<String, Element> complexTypes, Map<String, Element> simpleTypes,
            List<XsdElementSource> elementos, List<XsdAttributeSource> atributos,
            List<XsdEnumerationSource> enumeraciones) {

        int order = 1;
        for (Element child : children(particle, "element")) {
            parseElement(child, parentPath, order++, complexTypes, simpleTypes, elementos, atributos, enumeraciones);
        }
        for (Element nested : children(particle, "sequence")) {
            parseParticle(nested, parentPath, complexTypes, simpleTypes, elementos, atributos, enumeraciones);
        }
        if (!children(particle, "choice").isEmpty()) {
            throw new InfrastructureException("FACTUCORE.XSD.CHOICE.NO_SOPORTADO", parentPath);
        }
    }

    private void parseAttribute(Element attribute, String parentPath,
            Map<String, Element> simpleTypes, List<XsdAttributeSource> atributos) {
        String name = attr(attribute, "name");
        if (name == null || name.isBlank()) return;

        String typeName = localTypeName(attr(attribute, "type"));
        Element simpleType = firstChild(attribute, "simpleType");
        if (simpleType == null) simpleType = findType(simpleTypes, typeName);
        Restriction restriction = resolveRestriction(simpleType, simpleTypes);

        atributos.add(new XsdAttributeSource(parentPath, name, typeName,
                "required".equals(attr(attribute, "use")),
                firstNonBlank(attr(attribute, "default"), attr(attribute, "fixed")),
                restriction.pattern));
    }

    private Restriction resolveRestriction(Element simpleType, Map<String, Element> simpleTypes) {
        if (simpleType == null) return Restriction.empty();

        Element restriction = firstChild(simpleType, "restriction");
        if (restriction == null) {
            String base = localTypeName(attr(simpleType, "type"));
            Element referenced = findType(simpleTypes, base);
            return referenced == null ? Restriction.empty() : resolveRestriction(referenced, simpleTypes);
        }

        return new Restriction(
                facetInteger(restriction, "minLength"),
                facetInteger(restriction, "maxLength"),
                facetInteger(restriction, "totalDigits"),
                facetInteger(restriction, "fractionDigits"),
                facetDecimal(restriction, "minInclusive"),
                facetDecimal(restriction, "maxInclusive"),
                facetValue(restriction, "pattern"));
    }

    private void parseEnumerations(Element simpleType, String path, List<XsdEnumerationSource> enumeraciones) {
        Element restriction = firstChild(simpleType, "restriction");
        if (restriction == null) return;
        int order = 1;
        for (Element enumeration : children(restriction, "enumeration")) {
            String value = attr(enumeration, "value");
            if (value != null) enumeraciones.add(new XsdEnumerationSource(path, value, null, order++));
        }
    }

    private static Map<String, Element> indexChildren(Element parent, String localName) {
        Map<String, Element> result = new HashMap<>();
        for (Element child : children(parent, localName)) {
            String name = attr(child, "name");
            if (name != null && !name.isBlank()) result.put(name, child);
        }
        return result;
    }

    private static Element findType(Map<String, Element> types, String name) {
        return name == null ? null : types.get(name);
    }

    private static String localTypeName(String value) {
        if (value == null || value.isBlank()) return null;
        int separator = value.indexOf(':');
        return separator >= 0 ? value.substring(separator + 1) : value;
    }

    private static Integer integerAttr(Element element, String name, int defaultValue) {
        String value = attr(element, name);
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    private static Integer maxOccurs(Element element) {
        String value = attr(element, "maxOccurs");
        if (value == null || value.isBlank() || "1".equals(value)) return 1;
        if ("unbounded".equals(value)) return null;
        return Integer.valueOf(value);
    }

    private static Integer facetInteger(Element restriction, String name) {
        String value = facetValue(restriction, name);
        return value == null ? null : Integer.valueOf(value);
    }

    private static BigDecimal facetDecimal(Element restriction, String name) {
        String value = facetValue(restriction, name);
        return value == null ? null : new BigDecimal(value);
    }

    private static String facetValue(Element restriction, String name) {
        Element facet = firstChild(restriction, name);
        return facet == null ? null : attr(facet, "value");
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static String attr(Element element, String name) {
        String value = element.getAttribute(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static Element firstElement(Element parent, String localName) {
        for (Element child : children(parent, localName)) return child;
        return null;
    }

    private static List<Element> children(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && XSD_NS.equals(element.getNamespaceURI())
                    && localName.equals(element.getLocalName())) result.add(element);
        }
        return result;
    }

    private static Element firstChild(Element parent, String localName) {
        return firstElement(parent, localName);
    }

    private record Restriction(Integer minLength, Integer maxLength, Integer totalDigits,
            Integer fractionDigits, BigDecimal minInclusive, BigDecimal maxInclusive, String pattern) {
        private static Restriction empty() {
            return new Restriction(null, null, null, null, null, null, null);
        }
    }
}
