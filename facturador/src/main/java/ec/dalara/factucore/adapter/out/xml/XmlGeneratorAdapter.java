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

    private final FactuCoreSourcePort factuCoreSourcePort;

    public XmlGeneratorAdapter(FactuCoreSourcePort factuCoreSourcePort) {
        this.factuCoreSourcePort = factuCoreSourcePort;
    }

    @Override
    public String generar(DocumentDefinitionModel definition, Map<String, Object> datos, Map<String, Object> contexto) {
        if (definition == null) {
            throw new ApplicationException("FACTUCORE.XML.DEFINITION.REQUERIDA");
        }
        if (datos == null) {
            throw new ApplicationException("FACTUCORE.XML.DATOS.REQUERIDOS");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document document = factory.newDocumentBuilder().newDocument();
            ElementoXsdModel raiz = obtenerRaiz(definition);
            String namespaceXml = definition.getVersion().getNamespaceXml();

            Element elementoRaiz = crearRaiz(document, raiz, definition, datos, contexto, namespaceXml);
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
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore, String namespaceXml) {

        MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);
        Object valor = mapeo == null ? contextoJson
                : resolverValor(mapeo, contextoJson, contextoJson, contextoFactuCore, null);

        Element elemento = crearElementoXml(document, definicion.getNombre(), namespaceXml);

        Object contextoLocal = valor == null ? contextoJson : valor;
        aplicarAtributos(elemento, definicion, definition, contextoLocal, contextoJson, contextoFactuCore,
                obtenerRutaOrigen(mapeo, null));

        if (mapeo != null && esValorSimple(valor) && valor != null) {
            elemento.setTextContent(convertirValor(valor));
        }

        Map<String, Object> mapaHijos = convertirMapa(contextoLocal);
        agregarHijos(document, elemento, definicion, mapaHijos, definition, contextoJson, contextoFactuCore,
                namespaceXml, obtenerRutaOrigen(mapeo, null));

        return elemento;
    }

    private Element crearElemento(Document document, ElementoXsdModel definicion, Object contextoActual,
            DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, String namespaceXml, String rutaOrigenPadre) {

        MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);
        Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore, rutaOrigenPadre);

        if (valor instanceof Iterable<?> iterable) {
            List<Object> valores = convertirIterable(iterable);
            if (valores.isEmpty()) {
                return crearElementoConValor(document, definicion, contextoActual, definition, contextoJson,
                        contextoFactuCore, namespaceXml, rutaOrigenPadre, null);
            }

            Element primero = null;
            for (Object item : valores) {
                Element elemento = crearElementoConValor(document, definicion, item, definition, contextoJson,
                        contextoFactuCore, namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), item);
                if (primero == null) {
                    primero = elemento;
                } else {
                    Element parent = primero.getParentNode() instanceof Element parentElement ? parentElement : null;
                    if (parent != null) {
                        parent.appendChild(elemento);
                    }
                }
            }
            return primero;
        }

        return crearElementoConValor(document, definicion, valor, definition, contextoJson, contextoFactuCore,
                namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), contextoActual);
    }

    private Element crearElementoConValor(Document document, ElementoXsdModel definicion, Object valor,
            DocumentDefinitionModel definition, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, String namespaceXml, String rutaOrigenPadre,
            Object contextoActual) {

        if (valor == null && !tieneHijos(definicion, definition)) {
            return null;
        }

        Element elemento = crearElementoXml(document, definicion.getNombre(), namespaceXml);

        aplicarAtributos(elemento, definicion, definition, contextoActual, contextoJson, contextoFactuCore,
                rutaOrigenPadre);

        if (esValorSimple(valor)) {
            if (valor != null) {
                elemento.setTextContent(convertirValor(valor));
            }
            return elemento;
        }

        Map<String, Object> contextoLocal = convertirMapa(valor);
        agregarHijos(document, elemento, definicion, contextoLocal, definition, contextoJson, contextoFactuCore,
                namespaceXml, obtenerRutaOrigenActual(definicion, definition, rutaOrigenPadre));

        return elemento;
    }

    private void agregarHijos(Document document, Element padre, ElementoXsdModel definicion,
            Map<String, Object> contextoLocal, DocumentDefinitionModel definition,
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore, String namespaceXml,
            String rutaOrigenPadre) {

        for (ElementoXsdModel hijo : obtenerHijos(definicion, definition)) {
            MapeoXsdModel mapeo = obtenerMapeoElemento(hijo, definition);
            if (mapeo == null) {
                continue;
            }

            Object valor = resolverValor(mapeo, contextoLocal, contextoJson, contextoFactuCore, rutaOrigenPadre);

            if (valor instanceof Iterable<?> iterable) {
                for (Object item : convertirIterable(iterable)) {
                    Element elementoHijo = crearElementoConValor(document, hijo, item, definition, contextoJson,
                            contextoFactuCore, namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), item);
                    if (elementoHijo != null) {
                        padre.appendChild(elementoHijo);
                    }
                }
                continue;
            }

            Element elementoHijo = crearElementoConValor(document, hijo, valor, definition, contextoJson,
                    contextoFactuCore, namespaceXml, obtenerRutaOrigen(mapeo, rutaOrigenPadre), valor);
            if (elementoHijo != null) {
                padre.appendChild(elementoHijo);
            }
        }
    }

    private Object resolverValor(MapeoXsdModel mapeo, Object contextoActual, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, String rutaOrigenPadre) {

        if (mapeo == null || mapeo.getOrigen() == null || mapeo.getOrigen().isBlank()) {
            return null;
        }

        String tipoOrigen = mapeo.getTipoOrigen();
        String origen = mapeo.getOrigen();

        if ("FACTUCORE".equalsIgnoreCase(tipoOrigen)) {
            return factuCoreSourcePort.resolver(origen, contextoFactuCore).resultado();
        }

        if ("GENERADO".equalsIgnoreCase(tipoOrigen)) {
            return obtenerRuta(contextoFactuCore, origen);
        }

        if (!"JSON".equalsIgnoreCase(tipoOrigen)) {
            throw new ApplicationException("FACTUCORE.MAPEO_XSD.TIPO_ORIGEN_INVALIDO");
        }

        String rutaRelativa = obtenerRutaRelativa(origen, rutaOrigenPadre);
        Object valor = obtenerRuta(convertirMapa(contextoActual), rutaRelativa);

        if (valor != null) {
            return valor;
        }

        return obtenerRuta(contextoJson, origen);
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
            String rutaOrigenPadre) {

        definition.getAtributos().stream()
                .filter(atributo -> Objects.equals(atributo.getElementoXsdId(), definicion.getId()))
                .forEach(atributo -> {
                    MapeoXsdModel mapeo = obtenerMapeoAtributo(atributo, definition);
                    Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore,
                            rutaOrigenPadre);

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