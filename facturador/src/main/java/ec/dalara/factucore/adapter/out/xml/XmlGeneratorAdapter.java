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

			Element elementoRaiz = crearElemento(document, raiz, datos, definition, datos, contexto, namespaceXml, null);
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

	private Element crearElemento(Document document, ElementoXsdModel definicion, Object contextoActual,
			DocumentDefinitionModel definition, Map<String, Object> contextoJson,
			Map<String, Object> contextoFactuCore, String namespaceXml, String rutaOrigenPadre) {

		MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);
		Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore, rutaOrigenPadre);
		String rutaOrigenActual = rutaOrigenPadre;

		if (mapeo != null && "JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
			rutaOrigenActual = mapeo.getOrigen();
		}

		Element elemento = crearElementoXml(document, definicion.getNombre(), namespaceXml);

		aplicarAtributos(elemento, definicion, definition, contextoActual, contextoJson, contextoFactuCore,
				rutaOrigenActual);

		if (esValorSimple(valor)) {
			if (valor != null) {
				elemento.setTextContent(convertirValor(valor));
				return elemento;
			}

			if (!tieneHijos(definicion, definition)) {
				return null;
			}
		}

		if (valor instanceof Iterable<?> iterable) {
			for (Object item : iterable) {
				Element elementoItem = crearElementoConContexto(document, definicion, item, definition, contextoJson,
						contextoFactuCore, namespaceXml, rutaOrigenActual);
				if (elementoItem != null) {
					return elementoItem;
				}
			}
		}

		Map<String, Object> contextoLocal = convertirMapa(valor);
		agregarHijos(document, elemento, definicion, contextoLocal, definition, contextoJson, contextoFactuCore,
				namespaceXml, rutaOrigenActual);

		return elemento;
	}

	private Element crearElementoConContexto(Document document, ElementoXsdModel definicion, Object contextoActual,
			DocumentDefinitionModel definition, Map<String, Object> contextoJson,
			Map<String, Object> contextoFactuCore, String namespaceXml, String rutaOrigenPadre) {

		MapeoXsdModel mapeo = obtenerMapeoElemento(definicion, definition);
		Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore, rutaOrigenPadre);
		String rutaOrigenActual = rutaOrigenPadre;

		if (mapeo != null && "JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
			rutaOrigenActual = mapeo.getOrigen();
		}

		Element elemento = crearElementoXml(document, definicion.getNombre(), namespaceXml);

		aplicarAtributos(elemento, definicion, definition, contextoActual, contextoJson, contextoFactuCore,
				rutaOrigenActual);

		if (esValorSimple(valor)) {
			if (valor != null) {
				elemento.setTextContent(convertirValor(valor));
			} else if (!tieneHijos(definicion, definition)) {
				return null;
			}

			if (valor != null || !tieneHijos(definicion, definition)) {
				return elemento;
			}
		}

		if (valor instanceof Iterable<?> iterable) {
			for (Object item : iterable) {
				Element itemElemento = crearElementoConContexto(document, definicion, item, definition, contextoJson,
						contextoFactuCore, namespaceXml, rutaOrigenActual);
				if (itemElemento != null) {
					return itemElemento;
				}
			}
		}

		Map<String, Object> contextoLocal = convertirMapa(valor);
		agregarHijos(document, elemento, definicion, contextoLocal, definition, contextoJson, contextoFactuCore,
				namespaceXml, rutaOrigenActual);

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

			Object valorHijo = resolverValor(mapeo, contextoLocal, contextoJson, contextoFactuCore, rutaOrigenPadre);

			if (valorHijo == null && !tieneHijos(hijo, definition)) {
				continue;
			}

			if (valorHijo instanceof Iterable<?> iterable) {
				for (Object item : iterable) {
					Element elementoHijo = crearElementoConContexto(document, hijo, item, definition, contextoJson,
							contextoFactuCore, namespaceXml, obtenerRutaJson(mapeo, rutaOrigenPadre));
					if (elementoHijo != null) {
						padre.appendChild(elementoHijo);
					}
				}
				continue;
			}

			Element elementoHijo = crearElementoConContexto(document, hijo, valorHijo, definition, contextoJson,
					contextoFactuCore, namespaceXml, obtenerRutaJson(mapeo, rutaOrigenPadre));
			if (elementoHijo != null) {
				padre.appendChild(elementoHijo);
			}
		}
	}

	private Object resolverValor(MapeoXsdModel mapeo, Object contextoActual, Map<String, Object> contextoJson,
			Map<String, Object> contextoFactuCore, String rutaOrigenPadre) {

		if (mapeo == null) {
			return null;
		}

		String tipoOrigen = mapeo.getTipoOrigen();
		String ruta = mapeo.getOrigen();

		if (ruta == null || ruta.isBlank()) {
			return null;
		}

		if ("FACTUCORE".equalsIgnoreCase(tipoOrigen)) {
			return factuCoreSourcePort.resolver(ruta, contextoFactuCore).resultado();
		}

		if ("GENERADO".equalsIgnoreCase(tipoOrigen)) {
			return obtenerRuta(contextoFactuCore, ruta);
		}

		String rutaRelativa = obtenerRutaRelativa(ruta, rutaOrigenPadre);
		Object valor = obtenerRuta(convertirMapa(contextoActual), rutaRelativa);

		if (valor != null) {
			return valor;
		}

		if (!Objects.equals(rutaRelativa, ruta)) {
			valor = obtenerRuta(convertirMapa(contextoActual), ruta);
			if (valor != null) {
				return valor;
			}
		}

		return obtenerRuta(contextoJson, ruta);
	}

	private String obtenerRutaJson(MapeoXsdModel mapeo, String rutaOrigenPadre) {
		if (mapeo == null || !"JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
			return rutaOrigenPadre;
		}
		return mapeo.getOrigen();
	}

	private String obtenerRutaRelativa(String ruta, String rutaPadre) {
		if (rutaPadre == null || rutaPadre.isBlank()) {
			return ruta;
		}

		if (ruta.equals(rutaPadre)) {
			return "";
		}

		String prefijo = rutaPadre + ".";
		if (ruta.startsWith(prefijo)) {
			return ruta.substring(prefijo.length());
		}

		return ruta;
	}

	private Element crearElementoXml(Document document, String nombre, String namespaceXml) {
		if (namespaceXml == null || namespaceXml.isBlank()) {
			return document.createElement(nombre);
		}

		return document.createElementNS(namespaceXml, nombre);
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
		return definition.getMapeos().stream().filter(MapeoXsdModel::esElemento)
				.filter(mapeo -> Objects.equals(mapeo.getElementoXsdId(), elemento.getId())).findFirst().orElse(null);
	}

	private void aplicarAtributos(Element elemento, ElementoXsdModel definicion, DocumentDefinitionModel definition,
			Object contextoActual, Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
			String rutaOrigenPadre) {

		definition.getAtributos().stream()
				.filter(atributo -> Objects.equals(atributo.getElementoXsdId(), definicion.getId()))
				.forEach(atributo -> aplicarAtributo(elemento, atributo, definition, contextoActual, contextoJson,
						contextoFactuCore, rutaOrigenPadre));
	}

	private void aplicarAtributo(Element elemento, AtributoXsdModel atributo, DocumentDefinitionModel definition,
			Object contextoActual, Map<String, Object> contextoJson, Map<String, Object> contextoFactuCore,
			String rutaOrigenPadre) {

		MapeoXsdModel mapeo = definition.getMapeos().stream().filter(MapeoXsdModel::esAtributo)
				.filter(item -> Objects.equals(item.getAtributoXsdId(), atributo.getId())).findFirst().orElse(null);

		Object valor = resolverValor(mapeo, contextoActual, contextoJson, contextoFactuCore, rutaOrigenPadre);

		if (valor != null) {
			elemento.setAttribute(atributo.getNombre(), convertirValor(valor));
			return;
		}

		if (atributo.getValorPredeterminado() != null) {
			elemento.setAttribute(atributo.getNombre(), atributo.getValorPredeterminado());
		}
	}

	private Object obtenerRuta(Map<String, Object> datos, String ruta) {
		if (datos == null || ruta == null || ruta.isBlank()) {
			return null;
		}

		if (datos.containsKey(ruta)) {
			return datos.get(ruta);
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

				if (resultado instanceof Iterable<?> resultadosAnidados) {
					resultados.addAll((List<?>) convertirIterable(resultadosAnidados));
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