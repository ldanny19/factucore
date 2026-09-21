package ec.dalara.factucore.adapter.out.xml;

import ec.dalara.factucore.application.ApplicationException;
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
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

@Component
public class XmlGeneratorAdapter implements XmlGeneratorPort {

    @Override
    public String generar(
            DocumentDefinitionModel definition,
            Map<String, Object> datos
    ) {
        if (definition == null) {
            throw new ApplicationException(
                    "FACTUCORE.XML.DEFINITION.REQUERIDA"
            );
        }

        if (datos == null) {
            throw new ApplicationException(
                    "FACTUCORE.XML.DATOS.REQUERIDOS"
            );
        }

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            Document document =
                    factory.newDocumentBuilder().newDocument();

            Element raiz =
                    crearElementoRaiz(
                            document,
                            definition,
                            datos
                    );

            document.appendChild(raiz);

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

        } catch (ApplicationException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.GENERACION.ERROR"
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
                                new ApplicationException(
                                        "FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO"
                                )
                        );

        Element elemento =
                crearElemento(
                        document,
                        raiz,
                        datos,
                        definition
                );

        return elemento;
    }

    private Element crearElemento(
            Document document,
            ElementoXsdModel definicion,
            Map<String, Object> datos,
            DocumentDefinitionModel definition
    ) {
        Element elemento =
                document.createElement(
                        definicion.getNombre()
                );

        Object valor =
                obtenerValor(
                        datos,
                        definicion.getNombre()
                );

        if (valor != null
                && !esEstructura(valor)) {

            elemento.setTextContent(
                    String.valueOf(valor)
            );
        }

        definition.getElementos()
                .stream()
                .filter(hijo ->
                        Objects.equals(
                                hijo.getElementoPadreId(),
                                definicion.getId()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                ElementoXsdModel::getOrden,
                                Comparator.nullsLast(
                                        Integer::compareTo
                                )
                        )
                )
                .forEach(hijo -> {

                    Object valorHijo =
                            obtenerValor(
                                    datos,
                                    hijo.getNombre()
                            );

                    if (valorHijo == null) {
                        return;
                    }

                    agregarValor(
                            document,
                            elemento,
                            hijo,
                            valorHijo,
                            definition
                    );
                });

        return elemento;
    }

    private void agregarValor(
            Document document,
            Element padre,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition
    ) {
        if (valor instanceof Iterable<?> iterable) {

            for (Object item : iterable) {

                Element elemento =
                        crearElementoConValor(
                                document,
                                definicion,
                                item,
                                definition
                        );

                padre.appendChild(elemento);
            }

            return;
        }

        Element elemento =
                crearElementoConValor(
                        document,
                        definicion,
                        valor,
                        definition
                );

        padre.appendChild(elemento);
    }

    private Element crearElementoConValor(
            Document document,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition
    ) {
        Element elemento =
                document.createElement(
                        definicion.getNombre()
                );

        if (!esEstructura(valor)) {

            elemento.setTextContent(
                    String.valueOf(valor)
            );
        }

        definition.getElementos()
                .stream()
                .filter(hijo ->
                        Objects.equals(
                                hijo.getElementoPadreId(),
                                definicion.getId()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                ElementoXsdModel::getOrden,
                                Comparator.nullsLast(
                                        Integer::compareTo
                                )
                        )
                )
                .forEach(hijo -> {

                    Object valorHijo =
                            obtenerValor(
                                    datosDeEstructura(valor),
                                    hijo.getNombre()
                            );

                    if (valorHijo == null) {
                        return;
                    }

                    agregarValor(
                            document,
                            elemento,
                            hijo,
                            valorHijo,
                            definition
                    );
                });

        return elemento;
    }

    private Object obtenerValor(
            Map<String, Object> datos,
            String nombre
    ) {
        return datos.get(nombre);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> datosDeEstructura(
            Object valor
    ) {
        if (valor instanceof Map<?, ?> mapa) {
            return (Map<String, Object>) mapa;
        }

        return Map.of();
    }

    private boolean esEstructura(
            Object valor
    ) {
        return valor instanceof Map<?, ?>
                || valor instanceof Iterable<?>;
    }
}