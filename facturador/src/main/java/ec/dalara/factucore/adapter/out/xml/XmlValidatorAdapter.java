package ec.dalara.factucore.adapter.out.xml;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class XmlValidatorAdapter implements XmlValidatorPort {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlValidatorAdapter.class);

	private final XsdSchemaBuilder schemaBuilder;

	@Override
	public void validar(String xml, DocumentDefinitionModel definition) {
		if (xml == null || xml.isBlank()) {
			registrarErrorSinTag("FACTUCORE.XML.VALIDACION.XML.REQUERIDO");
		}

		if (definition == null) {
			registrarErrorSinTag("FACTUCORE.XML.VALIDACION.DEFINITION.REQUERIDA");
		}

		try {
			Document document = parsearXml(xml);

			validarEstructuraBasica(document, definition);

			validarElementos(document.getDocumentElement(), null, definition);

			validarAtributos(document.getDocumentElement(), definition);

			validarContraEsquema(xml, definition);

		} catch (ApplicationException exception) {
			throw exception;

		} catch (Exception exception) {
			String tag = documentTag(xml);
			LOGGER.error("Validacion XML/XSD fallida: codigo={}, tag={}, detalle={}",
					"FACTUCORE.XML.VALIDACION.ERROR", tag, exception.getMessage(), exception);
			throw new ApplicationException("FACTUCORE.XML.VALIDACION.ERROR", exception);
		}
	}

	private void validarContraEsquema(String xml, DocumentDefinitionModel definition) throws Exception {
		Schema schema = schemaBuilder.construir(definition);
		Validator validator = schema.newValidator();
		validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		validator.validate(new StreamSource(new StringReader(xml)));
	}

	private Document parsearXml(String xml) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);

		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);

		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);

		return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
	}

	private void validarEstructuraBasica(Document document, DocumentDefinitionModel definition) {
		ElementoXsdModel raiz = obtenerRaiz(definition);

		org.w3c.dom.Element elementoRaiz = document.getDocumentElement();

		if (!Objects.equals(
				elementoRaiz.getLocalName() != null ? elementoRaiz.getLocalName() : elementoRaiz.getNodeName(),
				raiz.getNombre())) {
			registrarError("FACTUCORE.XML.VALIDACION.RAIZ.INVALIDA", elementoRaiz.getLocalName() != null ? elementoRaiz.getLocalName() : elementoRaiz.getNodeName(), null);
		}

		String namespaceEsperado = definition.getVersion().getNamespaceXml();

		if (namespaceEsperado != null && !namespaceEsperado.isBlank()
				&& !Objects.equals(namespaceEsperado, elementoRaiz.getNamespaceURI())) {

			registrarError("FACTUCORE.XML.VALIDACION.NAMESPACE.INVALIDO", elementoRaiz.getLocalName() != null ? elementoRaiz.getLocalName() : elementoRaiz.getNodeName(), null);
		}
	}

	private void validarElementos(org.w3c.dom.Element elementoXml, ElementoXsdModel definicionPadre,
			DocumentDefinitionModel definition) {
		ElementoXsdModel definicion = buscarDefinicionElemento(elementoXml, definicionPadre, definition);

		if (definicion == null) {
			registrarError("FACTUCORE.XML.VALIDACION.ELEMENTO.NO_DEFINIDO", elementoXml.getLocalName() != null ? elementoXml.getLocalName() : elementoXml.getNodeName(), null);
		}

		validarValorElemento(elementoXml, definicion, definition);

		List<ElementoXsdModel> hijosDefinidos = obtenerHijos(definicion, definition);

		for (ElementoXsdModel hijoDefinido : hijosDefinidos) {

			List<org.w3c.dom.Element> hijosXml = obtenerHijosXml(elementoXml, hijoDefinido.getNombre());

			validarOcurrencias(hijoDefinido, hijosXml.size(), definition);

			for (org.w3c.dom.Element hijoXml : hijosXml) {

				validarElementos(hijoXml, definicion, definition);
			}
		}

		validarHijosNoDefinidos(elementoXml, hijosDefinidos, definition);
	}

	private ElementoXsdModel buscarDefinicionElemento(org.w3c.dom.Element elementoXml, ElementoXsdModel padre,
			DocumentDefinitionModel definition) {
		return definition.getElementos().stream().filter(elemento -> {

			if (padre == null) {
				return elemento.getElementoPadreId() == null;
			}

			return Objects.equals(elemento.getElementoPadreId(), padre.getId());
		}).filter(elemento -> Objects.equals(elemento.getNombre(),
				elementoXml.getLocalName() != null ? elementoXml.getLocalName() : elementoXml.getNodeName()))
				.findFirst().orElse(null);
	}

	private void validarHijosNoDefinidos(org.w3c.dom.Element elementoXml, List<ElementoXsdModel> hijosDefinidos,
			DocumentDefinitionModel definition) {
		org.w3c.dom.NodeList hijos = elementoXml.getChildNodes();

		for (int i = 0; i < hijos.getLength(); i++) {

			org.w3c.dom.Node nodo = hijos.item(i);

			if (nodo.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
				continue;
			}

			org.w3c.dom.Element hijoXml = (org.w3c.dom.Element) nodo;

			String nombre = hijoXml.getLocalName() != null ? hijoXml.getLocalName() : hijoXml.getNodeName();

			boolean definido = hijosDefinidos.stream()
					.anyMatch(elemento -> Objects.equals(elemento.getNombre(), nombre));

			if (!definido) {
				registrarError("FACTUCORE.XML.VALIDACION.ELEMENTO.NO_DEFINIDO", nombre, null);
			}
		}
	}

	private void validarOcurrencias(ElementoXsdModel definicion, int cantidad, DocumentDefinitionModel definition) {
		Integer minimo = definicion.getMinOcurrencias();

		Integer maximo = definicion.getMaxOcurrencias();

		if (minimo != null && cantidad < minimo) {
			registrarError("FACTUCORE.XML.VALIDACION.OCURRENCIA.MINIMA", definicion, definition, cantidad, minimo);
		}

		if (maximo != null && cantidad > maximo) {

			registrarError("FACTUCORE.XML.VALIDACION.OCURRENCIA.MAXIMA", definicion, definition, cantidad, maximo);
		}

		if (Boolean.TRUE.equals(definicion.getObligatorio()) && cantidad == 0) {

			registrarError("FACTUCORE.XML.VALIDACION.ELEMENTO.REQUERIDO", definicion, definition, cantidad);
		}

		if (!Boolean.TRUE.equals(definicion.getRepetible()) && cantidad > 1) {

			registrarError("FACTUCORE.XML.VALIDACION.ELEMENTO.NO_REPETIBLE", definicion, definition, cantidad);
		}
	}

	private void validarValorElemento(org.w3c.dom.Element elementoXml, ElementoXsdModel definicion,
			DocumentDefinitionModel definition) {
		boolean tieneHijos = !obtenerHijos(definicion, definition).isEmpty();

		if (tieneHijos) {
			return;
		}

		String valor = elementoXml.getTextContent();

		if (valor == null) {
			valor = "";
		}

		validarLongitud(valor, definicion, definition);

		validarTipo(valor, definicion, definition);

		validarNumerico(valor, definicion, definition);

		validarPatron(valor, definicion, definition);

		validarEnumeracion(valor, definicion, definition);
	}

	private void validarLongitud(String valor, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		if (definicion.getLongitudMinima() != null && valor.length() < definicion.getLongitudMinima()) {

			registrarError("FACTUCORE.XML.VALIDACION.LONGITUD.MINIMA", definicion, definition, definicion.getLongitudMinima());
		}

		if (definicion.getLongitudMaxima() != null && valor.length() > definicion.getLongitudMaxima()) {

			registrarError("FACTUCORE.XML.VALIDACION.LONGITUD.MAXIMA", definicion, definition, definicion.getLongitudMaxima());
		}
	}

	private void validarTipo(String valor, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		String tipo = definicion.getTipoDato();

		if (tipo == null || tipo.isBlank()) {
			return;
		}

		try {
			switch (tipo.toLowerCase()) {

			case "string":
			case "normalizedstring":
			case "token":
			case "text":
				break;

			case "integer":
			case "int":
			case "long":
				Long.parseLong(valor);
				break;

			case "decimal":
			case "double":
			case "float":
				new BigDecimal(valor);
				break;

			case "boolean":
				if (!valor.equals("true") && !valor.equals("false") && !valor.equals("1") && !valor.equals("0")) {

					throw new IllegalArgumentException();
				}
				break;

			case "date":
				java.time.LocalDate.parse(valor);
				break;

			case "datetime":
			case "dateTime":
				java.time.LocalDateTime.parse(valor);
				break;

			default:
				break;
			}

		} catch (Exception exception) {
			registrarError("FACTUCORE.XML.VALIDACION.TIPO.INVALIDO", definicion, definition, valor);
		}
	}

	private void validarNumerico(String valor, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		if (definicion.getValorMinimo() == null && definicion.getValorMaximo() == null
				&& definicion.getDigitosTotales() == null && definicion.getDecimales() == null) {

			return;
		}

		BigDecimal numero;

		try {
			numero = new BigDecimal(valor);
		} catch (Exception exception) {
			registrarError("FACTUCORE.XML.VALIDACION.NUMERICO.INVALIDO", definicion, definition, valor);
		}

		if (definicion.getValorMinimo() != null && numero.compareTo(definicion.getValorMinimo()) < 0) {

			registrarError("FACTUCORE.XML.VALIDACION.VALOR.MINIMO", definicion, definition, definicion.getValorMinimo());
		}

		if (definicion.getValorMaximo() != null && numero.compareTo(definicion.getValorMaximo()) > 0) {

			registrarError("FACTUCORE.XML.VALIDACION.VALOR.MAXIMO", definicion, definition, definicion.getValorMaximo());
		}

		if (definicion.getDigitosTotales() != null) {

			int digitos = contarDigitos(numero);

			if (digitos > definicion.getDigitosTotales()) {

				registrarError("FACTUCORE.XML.VALIDACION.DIGITOS.TOTALES", definicion, definition, definicion.getDigitosTotales());
			}
		}

		if (definicion.getDecimales() != null) {

			int decimales = Math.max(0, numero.scale());

			if (decimales > definicion.getDecimales()) {

				registrarError("FACTUCORE.XML.VALIDACION.DECIMALES", definicion, definition, definicion.getDecimales());
			}
		}
	}

	private int contarDigitos(BigDecimal numero) {
		return numero.unscaledValue().abs().toString().length();
	}

	private void validarPatron(String valor, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		String patron = definicion.getPatron();

		if (patron == null || patron.isBlank()) {
			return;
		}

		if (!valor.matches(patron)) {
			registrarError("FACTUCORE.XML.VALIDACION.PATRON.INVALIDO", definicion, definition);
		}
	}

	private void validarEnumeracion(String valor, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		List<String> valoresPermitidos = definition.getEnumeraciones().stream()
				.filter(enumeracion -> Objects.equals(enumeracion.getElementoXsdId(), definicion.getId()))
				.map(EnumeracionXsdModel::getValor).toList();

		if (!valoresPermitidos.isEmpty() && !valoresPermitidos.contains(valor)) {

			registrarError("FACTUCORE.XML.VALIDACION.ENUMERACION.INVALIDA", definicion, definition, valor);
		}
	}

	private void validarAtributos(org.w3c.dom.Element elementoXml, DocumentDefinitionModel definition) {
		ElementoXsdModel definicion = definition.getElementos().stream()
				.filter(elemento -> Objects.equals(elemento.getNombre(),
						elementoXml.getLocalName() != null ? elementoXml.getLocalName() : elementoXml.getNodeName()))
				.findFirst().orElse(null);

		if (definicion == null) {
			return;
		}

		List<AtributoXsdModel> atributos = definition.getAtributos().stream()
				.filter(atributo -> Objects.equals(atributo.getElementoXsdId(), definicion.getId())).toList();

		for (AtributoXsdModel atributo : atributos) {

			boolean existe = elementoXml.hasAttribute(atributo.getNombre());

			if (Boolean.TRUE.equals(atributo.getObligatorio()) && !existe
					&& atributo.getValorPredeterminado() == null) {

				registrarErrorAtributo("FACTUCORE.XML.VALIDACION.ATRIBUTO.REQUERIDO", definicion, definition, atributo.getNombre());
			}

			if (!existe) {
				continue;
			}

			String valor = elementoXml.getAttribute(atributo.getNombre());

			validarAtributoTipo(valor, atributo, definicion, definition);

			validarAtributoPatron(valor, atributo, definicion, definition);
		}

		validarAtributosNoDefinidos(elementoXml, atributos, definicion, definition);
	}

	private void validarAtributosNoDefinidos(org.w3c.dom.Element elementoXml, List<AtributoXsdModel> atributos,
			ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		org.w3c.dom.NamedNodeMap atributosXml = elementoXml.getAttributes();

		for (int i = 0; i < atributosXml.getLength(); i++) {

			org.w3c.dom.Node atributoXml = atributosXml.item(i);

			String namespace = atributoXml.getNamespaceURI();

			if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
				continue;
			}

			boolean definido = atributos.stream()
					.anyMatch(atributo -> Objects.equals(atributo.getNombre(), atributoXml.getNodeName()));

			if (!definido) {
				registrarErrorAtributo("FACTUCORE.XML.VALIDACION.ATRIBUTO.NO_DEFINIDO", definicion, definition, atributoXml.getNodeName());
			}
		}
	}

	private void validarAtributoTipo(String valor, AtributoXsdModel atributo, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		String tipo = atributo.getTipoDato();

		if (tipo == null || tipo.isBlank()) {
			return;
		}

		try {
			switch (tipo.toLowerCase()) {

			case "string":
			case "normalizedstring":
			case "token":
			case "text":
				break;

			case "integer":
			case "int":
			case "long":
				Long.parseLong(valor);
				break;

			case "decimal":
			case "double":
			case "float":
				new BigDecimal(valor);
				break;

			case "boolean":
				if (!valor.equals("true") && !valor.equals("false") && !valor.equals("1") && !valor.equals("0")) {

					throw new IllegalArgumentException();
				}
				break;

			default:
				break;
			}

		} catch (Exception exception) {
			registrarErrorAtributo("FACTUCORE.XML.VALIDACION.ATRIBUTO.TIPO.INVALIDO", definicion, definition, atributo.getNombre(), valor);
		}
	}

	private void validarAtributoPatron(String valor, AtributoXsdModel atributo, ElementoXsdModel definicion, DocumentDefinitionModel definition) {
		if (atributo.getPatron() == null || atributo.getPatron().isBlank()) {
			return;
		}

		if (!valor.matches(atributo.getPatron())) {
			registrarErrorAtributo("FACTUCORE.XML.VALIDACION.ATRIBUTO.PATRON.INVALIDO", definicion, definition, atributo.getNombre(), valor);
		}
	}

	private ApplicationException registrarErrorSinTag(String codigo) {
		LOGGER.error("Validacion XML/XSD fallida: codigo={}, tag={}", codigo, "N/A");
		throw new ApplicationException(codigo);
	}

	private ApplicationException registrarError(String codigo, String tag, String ruta) {
		LOGGER.error("Validacion XML/XSD fallida: codigo={}, tag={}, ruta={}", codigo, tag, ruta);
		throw new ApplicationException(codigo);
	}

	private ApplicationException registrarError(String codigo, ElementoXsdModel definicion,
			DocumentDefinitionModel definition, Object... parametros) {
		String ruta = obtenerRuta(definicion, definition);
		LOGGER.error("Validacion XML/XSD fallida: codigo={}, tag={}, ruta={}, parametros={}", codigo,
				definicion.getNombre(), ruta, java.util.Arrays.toString(parametros));
		throw new ApplicationException(codigo, parametros);
	}

	private ApplicationException registrarErrorAtributo(String codigo, ElementoXsdModel definicion,
			DocumentDefinitionModel definition, Object... parametros) {
		String ruta = obtenerRuta(definicion, definition);
		LOGGER.error("Validacion XML/XSD fallida: codigo={}, tag={}, ruta={}, atributo={}", codigo,
				definicion.getNombre(), ruta, parametros.length > 0 ? parametros[0] : null);
		throw new ApplicationException(codigo, parametros);
	}

	private String documentTag(String xml) {
		try {
			org.w3c.dom.Element raiz = parsearXml(xml).getDocumentElement();
			return raiz.getLocalName() != null ? raiz.getLocalName() : raiz.getNodeName();
		} catch (Exception ignored) {
			return "N/A";
		}
	}

	private String obtenerRuta(ElementoXsdModel elemento, DocumentDefinitionModel definition) {
		if (elemento == null) {
			return null;
		}
		if (elemento.getElementoPadreId() == null) {
			return elemento.getNombre();
		}
		return definition.getElementos().stream()
				.filter(padre -> Objects.equals(padre.getId(), elemento.getElementoPadreId()))
				.findFirst()
				.map(padre -> obtenerRuta(padre, definition) + "." + elemento.getNombre())
				.orElse(elemento.getNombre());
	}

	private ElementoXsdModel obtenerRaiz(DocumentDefinitionModel definition) {
		String nombre = definition.getVersion().getElementoRaiz();

		return definition.getElementos().stream().filter(elemento -> elemento.getElementoPadreId() == null)
				.filter(elemento -> nombre == null || nombre.isBlank() || Objects.equals(elemento.getNombre(), nombre))
				.findFirst()
				.orElseThrow(() -> new ApplicationException("FACTUCORE.XML.VALIDACION.ELEMENTO_RAIZ.NO_DEFINIDO"));
	}

	private List<ElementoXsdModel> obtenerHijos(ElementoXsdModel padre, DocumentDefinitionModel definition) {
		return definition.getElementos().stream()
				.filter(elemento -> Objects.equals(elemento.getElementoPadreId(), padre.getId()))
				.sorted(java.util.Comparator.comparing(ElementoXsdModel::getOrden,
						java.util.Comparator.nullsLast(Integer::compareTo)))
				.toList();
	}

	private List<org.w3c.dom.Element> obtenerHijosXml(org.w3c.dom.Element padre, String nombre) {
		java.util.ArrayList<org.w3c.dom.Element> resultado = new java.util.ArrayList<>();

		org.w3c.dom.NodeList hijos = padre.getChildNodes();

		for (int i = 0; i < hijos.getLength(); i++) {

			org.w3c.dom.Node nodo = hijos.item(i);

			if (nodo.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
				continue;
			}

			org.w3c.dom.Element elemento = (org.w3c.dom.Element) nodo;

			String nombreXml = elemento.getLocalName() != null ? elemento.getLocalName() : elemento.getNodeName();

			if (Objects.equals(nombre, nombreXml)) {
				resultado.add(elemento);
			}
		}

		return resultado;
	}
}