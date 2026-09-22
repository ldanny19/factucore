package ec.dalara.factucore.adapter.out.xml;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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

            ElementoXsdModel raiz =
                    obtenerRaiz(
                            definition
                    );

            String namespaceXml =
                    definition.getVersion()
                            .getNamespaceXml();

            Element elementoRaiz =
                    crearElemento(
                            document,
                            raiz,
                            datos,
                            definition,
                            datos,
                            namespaceXml
                    );

            document.appendChild(elementoRaiz);

            return serializar(document);

        } catch (ApplicationException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new ApplicationException(
                    "FACTUCORE.XML.GENERACION.ERROR"
            );
        }
    }

    private ElementoXsdModel obtenerRaiz(
            DocumentDefinitionModel definition
    ) {
        String nombreRaizConfigurado =
                definition.getVersion()
                        .getElementoRaiz();

        List<ElementoXsdModel> raices =
                definition.getElementos()
                        .stream()
                        .filter(elemento ->
                                elemento.getElementoPadreId() == null
                        )
                        .filter(elemento ->
                                nombreRaizConfigurado == null
                                        || nombreRaizConfigurado.isBlank()
                                        || Objects.equals(
                                                elemento.getNombre(),
                                                nombreRaizConfigurado
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
                        .toList();

        if (raices.isEmpty()) {
            throw new ApplicationException(
                    "FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO"
            );
        }

        if (raices.size() > 1) {
            throw new ApplicationException(
                    "FACTUCORE.XML.ELEMENTO_RAIZ.MULTIPLE"
            );
        }

        return raices.get(0);
    }

    private Element crearElemento(
            Document document,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto,
            String namespaceXml
    ) {
        Element elemento =
                crearElementoXml(
                        document,
                        definicion.getNombre(),
                        namespaceXml
                );

        aplicarAtributos(
                elemento,
                definicion,
                definition,
                contexto
        );

        if (esValorSimple(valor)) {
            if (valor != null) {
                elemento.setTextContent(
                        convertirValor(valor)
                );
            }

            return elemento;
        }

        Map<String, Object> contextoLocal =
                convertirMapa(valor);

        List<ElementoXsdModel> hijos =
                obtenerHijos(
                        definicion,
                        definition
                );

        for (ElementoXsdModel hijo : hijos) {

            MapeoXsdModel mapeo =
                    obtenerMapeoElemento(
                            hijo,
                            definition
                    );

            if (mapeo == null) {
                continue;
            }

            Object valorHijo =
                    obtenerValor(
                            contextoLocal,
                            contexto,
                            mapeo.getRutaOrigen()
                    );

            if (valorHijo == null) {
                continue;
            }

            agregarHijo(
                    document,
                    elemento,
                    hijo,
                    valorHijo,
                    definition,
                    contexto,
                    namespaceXml
            );
        }

        return elemento;
    }

    private Element crearElementoXml(
            Document document,
            String nombre,
            String namespaceXml
    ) {
        if (namespaceXml == null
                || namespaceXml.isBlank()) {

            return document.createElement(nombre);
        }

        return document.createElementNS(
                namespaceXml,
                nombre
        );
    }

    private void agregarHijo(
            Document document,
            Element padre,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto,
            String namespaceXml
    ) {
        if (valor instanceof Iterable<?> iterable) {

            for (Object item : iterable) {

                Element hijo =
                        crearElemento(
                                document,
                                definicion,
                                item,
                                definition,
                                contexto,
                                namespaceXml
                        );

                padre.appendChild(hijo);
            }

            return;
        }

        Element hijo =
                crearElemento(
                        document,
                        definicion,
                        valor,
                        definition,
                        contexto,
                        namespaceXml
                );

        padre.appendChild(hijo);
    }

    private List<ElementoXsdModel> obtenerHijos(
            ElementoXsdModel padre,
            DocumentDefinitionModel definition
    ) {
        return definition.getElementos()
                .stream()
                .filter(elemento ->
                        Objects.equals(
                                elemento.getElementoPadreId(),
                                padre.getId()
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
                .toList();
    }

    private MapeoXsdModel obtenerMapeoElemento(
            ElementoXsdModel elemento,
            DocumentDefinitionModel definition
    ) {
        return definition.getMapeos()
                .stream()
                .filter(MapeoXsdModel::esElemento)
                .filter(mapeo ->
                        Objects.equals(
                                mapeo.getElementoXsdId(),
                                elemento.getId()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private void aplicarAtributos(
            Element elemento,
            ElementoXsdModel definicion,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto
    ) {
        definition.getAtributos()
                .stream()
                .filter(atributo ->
                        Objects.equals(
                                atributo.getElementoXsdId(),
                                definicion.getId()
                        )
                )
                .forEach(atributo ->
                        aplicarAtributo(
                                elemento,
                                atributo,
                                definition,
                                contexto
                        )
                );
    }

    private void aplicarAtributo(
            Element elemento,
            AtributoXsdModel atributo,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto
    ) {
        MapeoXsdModel mapeo =
                definition.getMapeos()
                        .stream()
                        .filter(MapeoXsdModel::esAtributo)
                        .filter(item ->
                                Objects.equals(
                                        item.getAtributoXsdId(),
                                        atributo.getId()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        Object valor = null;

        if (mapeo != null) {
            valor =
                    obtenerRuta(
                            contexto,
                            mapeo.getRutaOrigen()
                    );
        }

        if (valor != null) {
            elemento.setAttribute(
                    atributo.getNombre(),
                    convertirValor(valor)
            );

            return;
        }

        if (atributo.getValorPredeterminado() != null) {
            elemento.setAttribute(
                    atributo.getNombre(),
                    atributo.getValorPredeterminado()
            );
        }
    }

    private Object obtenerValor(
            Map<String, Object> contextoLocal,
            Map<String, Object> contextoGlobal,
            String ruta
    ) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        Object valor =
                obtenerRuta(
                        contextoLocal,
                        ruta
                );

        if (valor != null) {
            return valor;
        }

        return obtenerRuta(
                contextoGlobal,
                ruta
        );
    }

    private Object obtenerRuta(
            Map<String, Object> datos,
            String ruta
    ) {
        if (datos == null
                || ruta == null
                || ruta.isBlank()) {
            return null;
        }

        if (datos.containsKey(ruta)) {
            return datos.get(ruta);
        }

        String[] partes =
                ruta.split("\\.");

        return resolverRuta(
                datos,
                partes,
                0
        );
    }

    private Object resolverRuta(
            Object actual,
            String[] partes,
            int indice
    ) {
        if (actual == null) {
            return null;
        }

        if (indice >= partes.length) {
            return actual;
        }

        if (actual instanceof Map<?, ?> mapa) {

            Object siguiente =
                    mapa.get(partes[indice]);

            return resolverRuta(
                    siguiente,
                    partes,
                    indice + 1
            );
        }

        if (actual instanceof Iterable<?> iterable) {

            java.util.ArrayList<Object> resultados =
                    new java.util.ArrayList<>();

            for (Object item : iterable) {

                Object resultado =
                        resolverRuta(
                                item,
                                partes,
                                indice
                        );

                if (resultado instanceof Iterable<?> resultadosAnidados) {
                    for (Object resultadoAnidado :
                            resultadosAnidados) {

                        resultados.add(
                                resultadoAnidado
                        );
                    }

                } else if (resultado != null) {
                    resultados.add(resultado);
                }
            }

            if (resultados.isEmpty()) {
                return null;
            }

            return resultados;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertirMapa(
            Object valor
    ) {
        if (valor instanceof Map<?, ?> mapa) {
            return (Map<String, Object>) mapa;
        }

        return new LinkedHashMap<>();
    }

    private boolean esValorSimple(
            Object valor
    ) {
        return valor == null
                || (!(valor instanceof Map<?, ?>)
                && !(valor instanceof Iterable<?>));
    }

    private String convertirValor(
            Object valor
    ) {
        return String.valueOf(valor);
    }

    private String serializar(
            Document document
    ) throws Exception {

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        transformerFactory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        var transformer =
                transformerFactory.newTransformer();

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
    }
}