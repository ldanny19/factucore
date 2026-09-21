package ec.dalara.factucore.adapter.out.xml;

import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;

@Component
public class XmlGeneratorAdapter implements XmlGeneratorPort {

    @Override
    public String generar(
            DocumentDefinitionModel definition,
            Map<String, Object> datos
    ) {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            Document document =
                    factory.newDocumentBuilder().newDocument();

            Element root =
                    crearElementoRaiz(
                            document,
                            definition,
                            datos
                    );

            document.appendChild(root);

            Transformer transformer =
                    TransformerFactory
                            .newInstance()
                            .newTransformer();

            transformer.setOutputProperty(
                    OutputKeys.ENCODING,
                    "UTF-8"
            );

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION,
                    "no"
            );

            StringWriter writer =
                    new StringWriter();

            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(writer)
            );

            return writer.toString();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "FACTUCORE.XML.GENERACION.ERROR",
                    exception
            );
        }
    }

    private Element crearElementoRaiz(
            Document document,
            DocumentDefinitionModel definition,
            Map<String, Object> datos
    ) {
        ElementoXsdModel raiz =
                definition.getElementos()
                        .stream()
                        .filter(elemento ->
                                elemento.getElementoPadreId() == null
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO"
                                )
                        );

        Element elemento =
                document.createElement(
                        raiz.getNombre()
                );

        Object valor =
                obtenerValor(
                        datos,
                        raiz.getNombre()
                );

        if (valor != null) {
            elemento.setTextContent(
                    String.valueOf(valor)
            );
        }

        agregarHijos(
                document,
                elemento,
                raiz,
                definition,
                datos
        );

        return elemento;
    }

    private void agregarHijos(
            Document document,
            Element padre,
            ElementoXsdModel elementoPadre,
            DocumentDefinitionModel definition,
            Map<String, Object> datos
    ) {
        definition.getElementos()
                .stream()
                .filter(elemento ->
                        java.util.Objects.equals(
                                elemento.getElementoPadreId(),
                                elementoPadre.getId()
                        )
                )
                .sorted(
                        java.util.Comparator.comparing(
                                ElementoXsdModel::getOrden,
                                java.util.Comparator.nullsLast(
                                        Integer::compareTo
                                )
                        )
                )
                .forEach(elemento -> {

                    Object valor =
                            obtenerValor(
                                    datos,
                                    elemento.getNombre()
                            );

                    if (valor == null) {
                        return;
                    }

                    Element hijo =
                            document.createElement(
                                    elemento.getNombre()
                            );

                    hijo.setTextContent(
                            String.valueOf(valor)
                    );

                    padre.appendChild(hijo);

                    agregarHijos(
                            document,
                            hijo,
                            elemento,
                            definition,
                            datos
                    );
                });
    }

    private Object obtenerValor(
            Map<String, Object> datos,
            String nombre
    ) {
        if (datos == null || nombre == null) {
            return null;
        }

        return datos.get(nombre);
    }
}