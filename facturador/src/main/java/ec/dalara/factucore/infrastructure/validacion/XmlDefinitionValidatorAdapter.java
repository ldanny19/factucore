package ec.dalara.factucore.infrastructure.validacion;

import java.io.StringReader;
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

import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.validacion.ValidationViolation;
import ec.dalara.factucore.domain.validacion.ValidationResult;
import ec.dalara.factucore.infrastructure.InfrastructureException;

@Component
public class XmlDefinitionValidatorAdapter implements XmlValidatorPort {

    @Override
    public void validar(String xml, DocumentDefinitionModel definition) {
        if (xml == null || xml.isBlank())
            throw new InfrastructureException("FACTUCORE.VALIDACION.XML.REQUERIDO");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
            Element root = document.getDocumentElement();
            if (!definition.getVersion().getElementoRaiz().equals(root.getLocalName()))
                throw new InfrastructureException("FACTUCORE.VALIDACION.XML.RAIZ.INVALIDA", root.getNodeName());

            Map<Long, List<ElementoXsdModel>> hijos = new HashMap<>();
            for (ElementoXsdModel e : definition.getElementos())
                if (e.getElementoPadreId() != null) hijos.computeIfAbsent(e.getElementoPadreId(), k -> new ArrayList<>()).add(e);

            ElementoXsdModel raiz = definition.getElementos().stream()
                    .filter(e -> e.getElementoPadreId() == null && e.getNombre().equals(root.getLocalName()))
                    .findFirst()
                    .orElseThrow(() -> new InfrastructureException("FACTUCORE.VALIDACION.XML.DEFINICION.RAIZ.NO_ENCONTRADA"));

            validarHijos(root, raiz, hijos);
        } catch (InfrastructureException e) {
            throw e;
        } catch (Exception e) {
            throw new InfrastructureException("FACTUCORE.VALIDACION.XML.INVALIDO", e);
        }
    }

    private void validarHijos(Element padre, ElementoXsdModel modelo, Map<Long,List<ElementoXsdModel>> hijos) {
        List<ElementoXsdModel> definiciones = hijos.getOrDefault(modelo.getId(), List.of());
        for (ElementoXsdModel hijo : definiciones) {
            int ocurrencias = 0;
            for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
                if (n instanceof Element e && hijo.getNombre().equals(e.getLocalName())) ocurrencias++;
            int min = hijo.getMinOcurrencias() == null ? 0 : hijo.getMinOcurrencias();
            Integer max = hijo.getMaxOcurrencias();
            if (ocurrencias < min) throw new InfrastructureException("FACTUCORE.VALIDACION.OCURRENCIA.MINIMA", hijo.getNombre(), min);
            if (max != null && ocurrencias > max) throw new InfrastructureException("FACTUCORE.VALIDACION.OCURRENCIA.MAXIMA", hijo.getNombre(), max);
            Element encontrado = null;
            for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling())
                if (n instanceof Element e && hijo.getNombre().equals(e.getLocalName())) { encontrado = e; break; }
            if (encontrado != null && hijos.containsKey(hijo.getId())) validarHijos(encontrado, hijo, hijos);
        }
    }
}
