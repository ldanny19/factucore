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
                    obtenerRaiz(definition);

            Element elementoRaiz =
                    crearElemento(
                            document,
                            raiz,
                            datos,
                            definition,
                            datos
                    );

            document.appendChild(elementoRaiz);

            var transformerFactory =
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
        return definition.getElementos()
                .stream()
                .filter(elemento ->
                        elemento.getElementoPadreId() == null
                )
                .sorted(
                        Comparator.comparing(
                                ElementoXsdModel::getOrden,
                                Comparator.nullsLast(
                                        Integer::compareTo
                                )
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ApplicationException(
                                "FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO"
                        )
                );
    }

    private Element crearElemento(
            Document document,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto
    ) {
        Element elemento =
                document.createElement(
                        definicion.getNombre()
                );

        aplicarAtributos(
                elemento,
                definicion,
                definition,
                contexto
        );

        if (valor != null
                && !esEstructura(valor)) {

            elemento.setTextContent(
                    convertirValor(valor)
            );

            return elemento;
        }

        Map<String, Object> datosHijo =
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
                            contexto,
                            mapeo.getRutaOrigen(),
                            datosHijo
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
                    contexto
            );
        }

        return elemento;
    }

    private void agregarHijo(
            Document document,
            Element padre,
            ElementoXsdModel definicion,
            Object valor,
            DocumentDefinitionModel definition,
            Map<String, Object> contexto
    ) {
        if (valor instanceof Iterable<?> iterable) {

            for (Object item : iterable) {

                Element elemento =
                        crearElemento(
                                document,
                                definicion,
                                item,
                                definition,
                                contexto
                        );

                padre.appendChild(elemento);
            }

            return;
        }

        Element elemento =
                crearElemento(
                        document,
                        definicion,
                        valor,
                        definition,
                        contexto
                );

        padre.appendChild(elemento);
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
                                        obtenerIdAtributo(
                                                atributo,
                                                definition
                                        )
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (mapeo == null) {
            if (atributo.getValorPredeterminado() != null) {
                elemento.setAttribute(
                        atributo.getNombre(),
                        atributo.getValorPredeterminado()
                );
            }

            return;
        }

        Object valor =
                obtenerValor(
                        contexto,
                        mapeo.getRutaOrigen(),
                        contexto
                );

        if (valor != null) {
            elemento.setAttribute(
                    atributo.getNombre(),
                    convertirValor(valor)
            );
        } else if (atributo.getValorPredeterminado() != null) {
            elemento.setAttribute(
                    atributo.getNombre(),
                    atributo.getValorPredeterminado()
            );
        }
    }

    private Long obtenerIdAtributo(
            AtributoXsdModel atributo,
            DocumentDefinitionModel definition
    ) {
        return definition.getMapeos()
                .stream()
                .filter(MapeoXsdModel::esAtributo)
                .filter(mapeo ->
                        definition.getAtributos()
                                .stream()
                                .anyMatch(item ->
                                        item == atributo
                                                && Objects.equals(
                                                mapeo.getAtributoXsdId(),
                                                buscarIdAtributo(
                                                        atributo,
                                                        definition
                                                )
                                )
                        )
                )
                .map(MapeoXsdModel::getAtributoXsdId)
                .findFirst()
                .orElse(
                        buscarIdAtributo(
                                atributo,
                                definition
                        )
                );
    }

    private Long buscarIdAtributo(
            AtributoXsdModel atributo,
            DocumentDefinitionModel definition
    ) {
        /*
         * El modelo actual de AtributoXsdModel no contiene su propio ID.
         *
         * La relación de mapeo hacia atributos requiere que el modelo
         * conserve dicho identificador para resolver correctamente:
         *
         * ruta_origen -> atributo_xsd_id.
         *
         * Mientras el modelo no exponga ese ID, no es posible realizar
         * este enlace de forma segura.
         */
        return null;
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

    private Object obtenerValor(
            Map<String, Object> contexto,
            String ruta,
            Map<String, Object> contextoLocal
    ) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        Object valorLocal =
                obtenerRuta(
                        contextoLocal,
                        ruta
                );

        if (valorLocal != null) {
            return valorLocal;
        }

        return obtenerRuta(
                contexto,
                ruta
        );
    }

    private Object obtenerRuta(
            Map<String, Object> datos,
            String ruta
    ) {
        if (datos == null) {
            return null;
        }

        if (datos.containsKey(ruta)) {
            return datos.get(ruta);
        }

        String[] partes =
                ruta.split("\\.");

        Object actual = datos;

        for (String parte : partes) {

            if (!(actual instanceof Map<?, ?> mapa)) {
                return null;
            }

            actual =
                    mapa.get(parte);

            if (actual == null) {
                return null;
            }
        }

        return actual;
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

    private boolean esEstructura(
            Object valor
    ) {
        return valor instanceof Map<?, ?>
                || valor instanceof Iterable<?>;
    }

    private String convertirValor(
            Object valor
    ) {
        return String.valueOf(valor);
    }
}