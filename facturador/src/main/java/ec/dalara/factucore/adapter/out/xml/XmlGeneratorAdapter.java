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
            Element elementoRaiz = crearInstancia(document, raiz, contextoJson, contextoJson, definition,
                    contextoFactuCore, contextoGenerado, null);

            if (elementoRaiz == null) {
                throw new ApplicationException("FACTUCORE.XML.ELEMENTO_RAIZ.NO_GENERADO");
            }

            document.appendChild(elementoRaiz);
            return serializar(document);
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException("FACTUCORE.XML.GENERACION.ERROR");
        }
    }

    private ElementoXsdModel obtenerRaiz(DocumentDefinitionModel definition) {
        String nombreRaiz = definition.getVersion().getElementoRaiz();

        List<ElementoXsdModel> raices = definition.getElementos().stream()
                .filter(e -> e.getElementoPadreId() == null)
                .filter(e -> nombreRaiz == null || nombreRaiz.isBlank()
                        || Objects.equals(e.getNombre(), nombreRaiz))
                .sorted(Comparator.comparing(ElementoXsdModel::getOrden,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();

        if (raices.isEmpty()) {
            throw new ApplicationException("FACTUCORE.XML.ELEMENTO_RAIZ.NO_DEFINIDO");
        }
        if (raices.size() > 1) {
            throw new ApplicationException("FACTUCORE.XML.ELEMENTO_RAIZ.MULTIPLE");
        }
        return raices.get(0);
    }

    private Element crearInstancia(Document document, ElementoXsdModel elemento, Object contexto,
            Map<String, Object> contextoJson, DocumentDefinitionModel definition,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado, String rutaPadre) {

        MapeoXsdModel mapeo = obtenerMapeoElemento(elemento, definition);
        Object valor = resolverValor(mapeo, contexto, contextoJson, contextoFactuCore, contextoGenerado, rutaPadre);

        if (mapeo != null && valor == null && !tieneHijos(elemento, definition)) {
            return null;
        }
        if (mapeo == null && !tieneHijos(elemento, definition) && !tieneAtributos(elemento, definition)) {
            return null;
        }

        Element xml = crearElementoXml(document, elemento.getNombre(), definition.getVersion().getNamespaceXml());

        aplicarAtributos(xml, elemento, contexto, contextoJson, contextoFactuCore, contextoGenerado,
                definition, rutaPadre);

        Object contextoHijos = contexto;

        if (mapeo != null) {
            if (esValorSimple(valor)) {
                if (valor != null) {
                    xml.setTextContent(convertirValor(valor));
                }
                return xml;
            }
            contextoHijos = valor;
        } else if (esContenidoSimple(elemento, definition)) {
            Object contenido = obtenerContenidoSimple(contexto, contextoJson, contextoFactuCore, contextoGenerado,
                    elemento, definition, rutaPadre);
            if (contenido != null) {
                xml.setTextContent(convertirValor(contenido));
            }
        }

        agregarHijos(document, xml, elemento, contextoHijos, contextoJson, definition,
                contextoFactuCore, contextoGenerado, rutaPadre);

        return xml;
    }

    private void agregarHijos(Document document, Element padre, ElementoXsdModel elemento, Object contexto,
            Map<String, Object> contextoJson, DocumentDefinitionModel definition,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado, String rutaPadre) {

        String rutaActual = obtenerRutaOrigenElemento(elemento, definition);
        if (rutaActual == null) {
            rutaActual = rutaPadre;
        }

        Object contextoBase = contexto;
        if (contexto instanceof Map<?, ?> && rutaActual != null && esRutaJson(elemento, definition, rutaActual)) {
            String relativa = obtenerRutaRelativa(rutaActual, rutaPadre);
            Object resuelto = obtenerRuta(contexto, relativa);
            if (resuelto != null) {
                contextoBase = resuelto;
            }
        }

        for (ElementoXsdModel hijo : obtenerHijos(elemento, definition)) {
            if (Boolean.TRUE.equals(hijo.getRepetible())) {
                Object coleccion = obtenerColeccion(hijo, contextoBase, contextoJson, definition, rutaActual);
                if (!(coleccion instanceof Iterable<?> iterable)) {
                    continue;
                }
                for (Object item : iterable) {
                    Element xml = crearInstancia(document, hijo, item, contextoJson, definition,
                            contextoFactuCore, contextoGenerado, rutaActual == null ? null : rutaActual + "." + hijo.getNombre());
                    if (xml != null) {
                        padre.appendChild(xml);
                    }
                }
            } else {
                Object contextoHijo = resolverContextoHijo(hijo, contextoBase, contextoJson, definition, rutaActual);
                Element xml = crearInstancia(document, hijo, contextoHijo, contextoJson, definition,
                        contextoFactuCore, contextoGenerado, rutaActual);
                if (xml != null) {
                    padre.appendChild(xml);
                }
            }
        }
    }

    private Object resolverContextoHijo(ElementoXsdModel hijo, Object contexto,
            Map<String, Object> contextoJson, DocumentDefinitionModel definition, String rutaPadre) {
        String ruta = obtenerRutaOrigenElemento(hijo, definition);
        if (ruta == null || !esRutaJson(hijo, definition, ruta)) {
            return contexto;
        }

        Object valor = obtenerRuta(contexto, obtenerRutaRelativa(ruta, rutaPadre));
        if (valor == null) {
            valor = obtenerRuta(contextoJson, ruta);
        }
        return valor == null ? contexto : valor;
    }

    private Object obtenerColeccion(ElementoXsdModel elemento, Object contexto,
            Map<String, Object> contextoJson, DocumentDefinitionModel definition, String rutaPadre) {
        String ruta = obtenerRutaOrigenElemento(elemento, definition);
        if (ruta != null && esRutaJson(elemento, definition, ruta)) {
            Object valor = obtenerRuta(contexto, obtenerRutaRelativa(ruta, rutaPadre));
            if (valor instanceof Iterable<?>) {
                return valor;
            }
            valor = obtenerRuta(contextoJson, ruta);
            if (valor instanceof Iterable<?>) {
                return valor;
            }
        }
        return contexto instanceof Iterable<?> ? contexto : null;
    }

    private String obtenerRutaOrigenElemento(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        MapeoXsdModel directo = obtenerMapeoElemento(elemento, definition);
        if (directo != null && "JSON".equalsIgnoreCase(directo.getTipoOrigen())) {
            return directo.getOrigen();
        }

        for (MapeoXsdModel mapeo : definition.getMapeos()) {
            if (mapeo == null || !"JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
                continue;
            }

            Long destino = mapeo.esElemento() ? mapeo.getElementoXsdId() : obtenerElementoDeAtributo(mapeo, definition);
            if (!esDescendiente(destino, elemento.getId(), definition)) {
                continue;
            }

            String ruta = recortarRuta(mapeo.getOrigen(), elemento.getNombre());
            if (ruta != null) {
                return ruta;
            }
        }
        return null;
    }

    private boolean esRutaJson(ElementoXsdModel elemento, DocumentDefinitionModel definition, String ruta) {
        return definition.getMapeos().stream()
                .filter(Objects::nonNull)
                .filter(m -> "JSON".equalsIgnoreCase(m.getTipoOrigen()))
                .anyMatch(m -> m.getOrigen().equals(ruta) || m.getOrigen().startsWith(ruta + "."));
    }

    private boolean esDescendiente(Long destino, Long ancestro, DocumentDefinitionModel definition) {
        ElementoXsdModel actual = buscarElemento(destino, definition);
        while (actual != null) {
            if (Objects.equals(actual.getId(), ancestro)) {
                return true;
            }
            actual = buscarElemento(actual.getElementoPadreId(), definition);
        }
        return false;
    }

    private String recortarRuta(String ruta, String nombre) {
        if (ruta == null || nombre == null) {
            return null;
        }
        String token = "." + nombre;
        int posicion = ruta.lastIndexOf(token);
        if (posicion >= 0 && (posicion + token.length() == ruta.length()
                || ruta.charAt(posicion + token.length()) == '.')) {
            return ruta.substring(0, posicion + token.length());
        }
        return ruta.equals(nombre) || ruta.startsWith(nombre + ".") ? nombre : null;
    }

    private MapeoXsdModel obtenerMapeoElemento(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        return definition.getMapeos().stream()
                .filter(Objects::nonNull)
                .filter(MapeoXsdModel::esElemento)
                .filter(m -> Objects.equals(m.getElementoXsdId(), elemento.getId()))
                .findFirst()
                .orElse(null);
    }

    private Long obtenerElementoDeAtributo(MapeoXsdModel mapeo, DocumentDefinitionModel definition) {
        if (mapeo.getAtributoXsdId() == null) {
            return null;
        }
        return definition.getAtributos().stream()
                .filter(a -> Objects.equals(a.getId(), mapeo.getAtributoXsdId()))
                .map(AtributoXsdModel::getElementoXsdId)
                .findFirst()
                .orElse(null);
    }

    private void aplicarAtributos(Element xml, ElementoXsdModel elemento, Object contexto,
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
            Map<String, Object> contextoGenerado, DocumentDefinitionModel definition, String rutaPadre) {
        for (AtributoXsdModel atributo : definition.getAtributos()) {
            if (!Objects.equals(atributo.getElementoXsdId(), elemento.getId())) {
                continue;
            }

            MapeoXsdModel mapeo = definition.getMapeos().stream()
                    .filter(Objects::nonNull)
                    .filter(MapeoXsdModel::esAtributo)
                    .filter(m -> Objects.equals(m.getAtributoXsdId(), atributo.getId()))
                    .findFirst()
                    .orElse(null);

            Object valor = resolverValor(mapeo, contexto, contextoJson, contextoFactuCore, contextoGenerado,
                    rutaPadre);

            if (valor != null) {
                xml.setAttribute(atributo.getNombre(), convertirValor(valor));
            } else if (atributo.getValorPredeterminado() != null) {
                xml.setAttribute(atributo.getNombre(), atributo.getValorPredeterminado());
            }
        }
    }

    private Object resolverValor(MapeoXsdModel mapeo, Object contexto,
            Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
            Map<String, Object> contextoGenerado, String rutaPadre) {
        if (mapeo == null) {
            return null;
        }

        return switch (mapeo.getTipoOrigen().toUpperCase()) {
        case "FACTUCORE" -> factuCoreSourcePort.resolver(mapeo.getOrigen(), contextoFactuCore).resultado();
        case "GENERADO" -> obtenerRuta(contextoGenerado, mapeo.getOrigen());
        case "JSON" -> {
            Object valor = obtenerRuta(contexto, obtenerRutaRelativa(mapeo.getOrigen(), rutaPadre));
            if (valor == null) {
                valor = obtenerRuta(contextoJson, mapeo.getOrigen());
            }
            yield valor;
        }
        default -> throw new ApplicationException("FACTUCORE.MAPEO_XSD.TIPO_ORIGEN_INVALIDO");
        };
    }

    private boolean esContenidoSimple(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        return !tieneHijos(elemento, definition)
                && definition.getMapeos().stream()
                        .anyMatch(m -> m.esElemento() && Objects.equals(m.getElementoXsdId(), elemento.getId()))
                && definition.getAtributos().stream()
                        .anyMatch(a -> Objects.equals(a.getElementoXsdId(), elemento.getId()));
    }

    private Object obtenerContenidoSimple(Object contexto, Map<String, Object> contextoJson,
            Map<String, Object> contextoFactuCore, Map<String, Object> contextoGenerado,
            ElementoXsdModel elemento, DocumentDefinitionModel definition, String rutaPadre) {
        MapeoXsdModel mapeo = obtenerMapeoElemento(elemento, definition);
        return resolverValor(mapeo, contexto, contextoJson, contextoFactuCore, contextoGenerado, rutaPadre);
    }

    private ElementoXsdModel buscarElemento(Long id, DocumentDefinitionModel definition) {
        if (id == null) {
            return null;
        }
        return definition.getElementos().stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElse(null);
    }

    private List<ElementoXsdModel> obtenerHijos(ElementoXsdModel padre, DocumentDefinitionModel definition) {
        return definition.getElementos().stream()
                .filter(e -> Objects.equals(e.getElementoPadreId(), padre.getId()))
                .sorted(Comparator.comparing(ElementoXsdModel::getOrden,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private boolean tieneHijos(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        return definition.getElementos().stream()
                .anyMatch(e -> Objects.equals(e.getElementoPadreId(), elemento.getId()));
    }

    private boolean tieneAtributos(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
        return definition.getAtributos().stream()
                .anyMatch(a -> Objects.equals(a.getElementoXsdId(), elemento.getId()));
    }

        private String obtenerRutaRelativa(String origen, String rutaPadre) {
        if (origen == null || origen.isBlank()) {
            return origen;
        }
        if (rutaPadre == null || rutaPadre.isBlank()) {
            return origen;
        }
        if (origen.equals(rutaPadre)) {
            return "";
        }
        String prefijo = rutaPadre + ".";
        return origen.startsWith(prefijo) ? origen.substring(prefijo.length()) : origen;
    }

    private Object obtenerRuta(Object datos, String ruta) {
        if (datos == null) {
            return null;
        }
        if (ruta == null || ruta.isBlank()) {
            return datos;
        }
        Object actual = datos;
        for (String parte : ruta.split("\\.")) {
            if (!(actual instanceof Map<?, ?> mapa)) {
                return null;
            }
            actual = mapa.get(parte);
        }
        return actual;
    }

    private Map<String, Object> convertirMapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            Map<String, Object> resultado = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapa.entrySet()) {
                resultado.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return resultado;
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
        return namespaceXml == null || namespaceXml.isBlank()
                ? document.createElement(nombre)
                : document.createElementNS(namespaceXml, nombre);
    }

    private String serializar(Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        var transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}