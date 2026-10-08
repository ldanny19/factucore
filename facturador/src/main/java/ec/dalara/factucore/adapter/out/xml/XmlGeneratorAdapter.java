package ec.dalara.factucore.adapter.out.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.FactuCoreSourcePort;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;

@Component
public class XmlGeneratorAdapter implements XmlGeneratorPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlGeneratorAdapter.class);

    private final FactuCoreSourcePort factuCoreSourcePort;

    public XmlGeneratorAdapter(FactuCoreSourcePort factuCoreSourcePort) {
        this.factuCoreSourcePort = factuCoreSourcePort;
    }

    @Override
    public String generar(DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado) {
        if (definition == null) {
            throw new ApplicationException("FACTUCORE.XML.DEFINITION.REQUERIDA");
        }
        if (contextoJson == null) {
            throw new ApplicationException("FACTUCORE.XML.DATOS.REQUERIDOS");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document document = factory.newDocumentBuilder().newDocument();
            ElementoXsdModel raiz = obtenerRaiz(definition);
            String namespaceXml = definition.getVersion().getNamespaceXml();

            Element elementoRaiz = crearRaiz(document, raiz, definition, contextoJson, contextoFactuCore, contextoGenerado, namespaceXml);
            document.appendChild(elementoRaiz);

            return serializar(document);
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException("FACTUCORE.XML.GENERACION.ERROR");
        }
    }

    private ElementoXsdModel obtenerRaiz(DocumentDefinitionModel definition) {
        String nombreRaizConfigurado = definition.getVersion().getElementoRaiz();

        List<ElementoXsdModel> raices = definition.getElementos().stream()
                .filter(elemento -> elemento.getElementoPadreId() == null)
                .filter(elemento -> nombreRaizConfigurado == null || nombreRaizConfigurado.isBlank()
                        || Objects.equals(elemento.getNombre(), nombreRaizConfigurado))
                .sorted(Comparator.comparing(ElementoXsdModel::getOrden, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        if (raices.isEmpty()) {
            throw new ApplicationException("FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO");
        }
        if (raices.size() > 1) {
            throw new ApplicationException("FACTUCORE.XML.ELEMENTO_RAIZ.MULTIPLE");
        }

        return raices.get(0);
    }

    private Element crearRaiz(Document document, ElementoXsdModel definicion, DocumentDefinitionModel definition,
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado, String namespaceXml) {
        return crearElemento(document, definicion, contextoJson, definition, contextoJson, contextoFactuCore, contextoGenerado,
                namespaceXml, null);
    }

    private Element crearElemento(Document document, ElementoXsdModel definicion, Object contextoActual,
            DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado, String namespaceXml,
            String rutaOrigenPadre) {

        List<Element> elementos = crearElementos(document, definicion, contextoActual, definition, contextoJson,
                contextoFactuCore, contextoGenerado, namespaceXml, rutaOrigenPadre);

        return elementos.isEmpty() ? null : elementos.get(0);
    }

    private List<Element> crearElementos(Document document, ElementoXsdModel definicion, Object contextoActual,
            DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado,
            String namespaceXml, String rutaOrigenPadre) {

        MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);

        LOGGER.info(
                "MAPEO ELEMENTO XML: elementoId={}, elemento={}, totalMapeos={}, mapeoEncontrado={}, tipoOrigen={}, origen={}",
                definicion.getId(),
                definicion.getNombre(),
                definition.getMapeos() == null ? 0 : definition.getMapeos().size(),
                mapeo != null,
                mapeo == null ? null : mapeo.getTipoOrigen(),
                mapeo == null ? null : mapeo.getOrigen());

        Object valor = mapeo == null ? contextoActual
                : resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore, contextoGenerado,
                        rutaOrigenPadre);

        if (valor instanceof Iterable<?> iterable) {
            List<Element> elementos = new ArrayList<>();

            for (Object item : convertirIterable(iterable)) {
                Element elemento = crearElementoConValor(document, definicion, item, definition, contextoJson,
                        contextoFactuCore, contextoGenerado, namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), item);

                if (elemento != null) {
                    elementos.add(elemento);
                }
            }

            if (!elementos.isEmpty()) {
                return elementos;
            }
        }

        Element elemento = crearElementoConValor(document, definicion, valor, definition, contextoJson,
                contextoFactuCore, contextoGenerado, namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), valor);

        return elemento == null ? List.of() : List.of(elemento);
    }

    private Element crearElementoConValor(Document document, ElementoXsdModel definicion, Object valor,
            DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado,
            String namespaceXml, String rutaOrigenPadre, Object contextoActual) {

        if (valor == null && !tieneHijos(definicion, definition)) {
            return null;
        }

        Element elemento = crearElementoXml(document, definicion.getNombre(), namespaceXml);

        aplicarAtributos(elemento, definicion, definition, contextoActual, contextoJson, contextoFactuCore, contextoGenerado,
                rutaOrigenPadre);

        if (esValorSimple(valor)) {
            if (valor != null) {
                elemento.setTextContent(convertirValor(valor));
            }
            return elemento;
        }

        Map<String, Object> contextoLocal = convertirMapa(valor);
        agregarHijos(document, elemento, definicion, contextoLocal, definition, contextoJson, contextoFactuCore,
                contextoGenerado, namespaceXml, obtenerRutaOrigenActual(definicion, definition, rutaOrigenPadre));

        return elemento;
    }

    private void agregarHijos(Document document, Element padre, ElementoXsdModel definicion,
            Map<String, Object> contextoLocal, DocumentDefinitionModel definition,
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
            Map<String, Object> contextoGenerado, String namespaceXml, String rutaOrigenPadre) {

        for (ElementoXsdModel hijo : obtenerHijos(definicion, definition)) {
            List<Element> elementosHijo = crearElementos(document, hijo, contextoLocal, definition,
                    contextoJson, contextoFactuCore, contextoGenerado, namespaceXml, rutaOrigenPadre);

            for (Element elementoHijo : elementosHijo) {
                padre.appendChild(elementoHijo);
            }
        }
    }

    private Object resolverValor(MapeoXsdModel mapeo, Object contextoActual, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado, String rutaOrigenPadre) {

        if (mapeo == null || mapeo.getOrigen() == null || mapeo.getOrigen().isBlank()) {
            return null;
        }

        String tipoOrigen = mapeo.getTipoOrigen();
        String origen = mapeo.getOrigen();

        LOGGER.info("Resolviendo valor XML: tipoOrigen={}, origen={}, contextoJson={}, contextoFactuCore={}, contextoGenerado={}, contextoActual={}",
                tipoOrigen, origen, contextoJson, contextoFactuCore, contextoGenerado, contextoActual);

        Object valor;

        if ("FACTUCORE".equalsIgnoreCase(tipoOrigen)) {
            valor = factuCoreSourcePort.resolver(origen, contextoFactuCore).resultado();
        } else if ("GENERADO".equalsIgnoreCase(tipoOrigen)) {
            valor = obtenerRuta(contextoGenerado, origen);
        } else if ("JSON".equalsIgnoreCase(tipoOrigen)) {
            String rutaRelativa = obtenerRutaRelativa(origen, rutaOrigenPadre);
            valor = obtenerRuta(convertirMapa(contextoActual), rutaRelativa);

            if (valor == null) {
                valor = obtenerRuta(contextoJson, origen);
            }
        } else {
            throw new ApplicationException("FACTUCORE.MAPEO_XSD.TIPO_ORIGEN_INVALIDO");
        }

        LOGGER.info("Valor XML resuelto: tipoOrigen={}, origen={}, valor={}",
                tipoOrigen, origen, valor);

        return valor;
    }

    private String obtenerRutaOrigenActual(ElementoXsdModel definicion, DocumentDefinitionModel definition,
            String rutaOrigenPadre) {
        MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);
        return obtenerRutaOrigen(mapeo, rutaOrigenPadre);
    }

    private String obtenerRutaOrigen(MapeoXsdModel mapeo, String rutaOrigenPadre) {
        if (mapeo != null && "JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
            return mapeo.getOrigen();
        }
        return rutaOrigenPadre;
    }

    private String obtenerRutaRelativa(String origen, String rutaPadre) {
        if (rutaPadre == null || rutaPadre.isBlank()) {
            return origen;
        }
        if (origen.equals(rutaPadre)) {
            return "";
        }

        String prefijo = rutaPadre + ".";
        if (origen.startsWith(prefijo)) {
            return origen.substring(prefijo.length());
        }

        return origen;
    }

    private List<ElementoXsdModel> obtenerHijos(ElementoXsdModel padre, DocumentDefinitionModel definition) {
        return definition.getElementos().stream()
                .filter(elemento -> Objects.equals(elemento.getElementoPadreId(), padre.getId()))
                .sorted(Comparator.comparing(ElementoXsdModel::getOrden, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private boolean tieneHijos(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        return definition.getElementos().stream()
                .anyMatch(hijo -> Objects.equals(hijo.getElementoPadreId(), elemento.getId()));
    }

    private MapeoXsdModel obtenerMapeoElemento(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        LOGGER.info(
                "BUSCANDO MAPEO: elementoId={}, elemento={}, totalMapeos={}",
                elemento.getId(),
                elemento.getNombre(),
                definition.getMapeos() == null ? 0 : definition.getMapeos().size());

        return definition.getMapeos().stream()
                .filter(MapeoXsdModel::esElemento)
                .filter(mapeo -> Objects.equals(mapeo.getElementoXsdId(), elemento.getId()))
                .findFirst()
                .orElse(null);
    }

    private MapeoXsdModel obtenerMapeoAtributo(AtributoXsdModel atributo, DocumentDefinitionModel definition) {
        return definition.getMapeos().stream()
                .filter(MapeoXsdModel::esAtributo)
                .filter(mapeo -> Objects.equals(mapeo.getAtributoXsdId(), atributo.getId()))
                .findFirst()
                .orElse(null);
    }

    private void aplicarAtributos(Element elemento, ElementoXsdModel definicion, DocumentDefinitionModel definition,
            Object contextoActual, Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
            Map<String, Object> contextoGenerado, String rutaOrigenPadre) {

        definition.getAtributos().stream()
                .filter(atributo -> Objects.equals(atributo.getElementoXsdId(), definicion.getId()))
                .forEach(atributo -> {
                    MapeoXsdModel mapeo = obtenerMapeoAtributo(atributo, definition);
                    Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore,
                            contextoGenerado, rutaOrigenPadre);

                    if (valor != null) {
                        elemento.setAttribute(atributo.getNombre(), convertirValor(valor));
                    } else if (atributo.getValorPredeterminado() != null) {
                        elemento.setAttribute(atributo.getNombre(), atributo.getValorPredeterminado());
                    }
                });
    }

    private Object obtenerRuta(Map<String, Object> datos, String ruta) {
        if (datos == null || ruta == null || ruta.isBlank()) {
            return null;
        }
        return resolverRuta(datos, ruta.split("\\."), 0);
    }

    private Object resolverRuta(Object actual, String[] partes, int indice) {
        if (actual == null) {
            return null;
        }
        if (indice >= partes.length) {
            return actual;
        }

        if (actual instanceof Map<?, ?> mapa) {
            return resolverRuta(mapa.get(partes[indice]), partes, indice + 1);
        }

        if (actual instanceof Iterable<?> iterable) {
            List<Object> resultados = new ArrayList<>();
            for (Object item : iterable) {
                Object resultado = resolverRuta(item, partes, indice);
                if (resultado instanceof Iterable<?> anidado) {
                    resultados.addAll(convertirIterable(anidado));
                } else if (resultado != null) {
                    resultados.add(resultado);
                }
            }
            return resultados.isEmpty() ? null : resultados;
        }

        return null;
    }

    private List<Object> convertirIterable(Iterable<?> iterable) {
        List<Object> resultado = new ArrayList<>();
        for (Object item : iterable) {
            resultado.add(item);
        }
        return resultado;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertirMapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            return (Map<String, Object>) mapa;
        }
        return new LinkedHashMap<>();
    }

    private boolean esValorSimple(Object valor) {
        return valor == null || (!(valor instanceof Map<?, ?>) && !(valor instanceof Iterable<?>));
    }

    private String convertirValor(Object valor) {
        return String.valueOf(valor);
    }

    private Element crearElementoXml(Document document, String nombre, String namespaceXml) {
        if (namespaceXml == null || namespaceXml.isBlank()) {
            return document.createElement(nombre);
        }
        return document.createElementNS(namespaceXml, nombre);
    }

    private String serializar(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        var transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }
}