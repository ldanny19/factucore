package ec.dalara.factucore.adapter.out.sri;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.port.out.SriPort;
import ec.dalara.factucore.application.port.out.sri.SriCommunicationException;
import ec.dalara.factucore.application.port.out.sri.SriMensaje;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;

@Component
public class SriSoapAdapter implements SriPort {

	private static final Logger LOGGER = LoggerFactory.getLogger(SriSoapAdapter.class);
	private static final String SOAP_ENV_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

	private final SriProperties properties;
	private final MessageResolver messageResolver;
	private final HttpClient httpClient;
	private final DocumentBuilderFactory documentBuilderFactory;

	public SriSoapAdapter(SriProperties properties, MessageResolver messageResolver) {
		this.properties = properties;
		this.messageResolver = messageResolver;
		this.httpClient = HttpClient.newBuilder().build();
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setNamespaceAware(true);
		configurarParserSeguro(this.documentBuilderFactory);
	}

	@Override
	public SriResponse recibir(String xml) {
		if (xml == null || xml.isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_XML_REQUERIDO);
		}
		String url = obtenerUrlRecepcion();
		String soapRequest = construirSolicitudRecepcion(xml);
		String respuesta = ejecutarSolicitud(url, soapRequest, "recepcion");
		return procesarRespuestaRecepcion(respuesta);
	}

	@Override
	public SriResponse autorizar(String claveAcceso) {
		if (claveAcceso == null || claveAcceso.isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
		}
		String url = obtenerUrlAutorizacion();
		String soapRequest = construirSolicitudAutorizacion(claveAcceso);
		String respuesta = ejecutarSolicitud(url, soapRequest, "autorizacion");
		return procesarRespuestaAutorizacion(respuesta);
	}

	private String obtenerUrlRecepcion() {
		SriProperties.AmbienteProperties ambiente = obtenerAmbiente();
		if (ambiente.getRecepcionUrl() == null || ambiente.getRecepcionUrl().isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_URL_RECEPCION_NO_CONFIGURADA, properties.getAmbiente());
		}
		return ambiente.getRecepcionUrl();
	}

	private String obtenerUrlAutorizacion() {
		SriProperties.AmbienteProperties ambiente = obtenerAmbiente();
		if (ambiente.getAutorizacionUrl() == null || ambiente.getAutorizacionUrl().isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_URL_AUTORIZACION_NO_CONFIGURADA, properties.getAmbiente());
		}
		return ambiente.getAutorizacionUrl();
	}

	private SriProperties.AmbienteProperties obtenerAmbiente() {
		if (properties.getAmbiente() == null || properties.getAmbiente().isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_AMBIENTE_REQUERIDO);
		}
		return switch (properties.getAmbiente().trim().toUpperCase()) {
		case "PRUEBAS" -> properties.getPruebas();
		case "PRODUCCION" -> properties.getProduccion();
		default -> throw new ApplicationException(MessageCodes.SRI_AMBIENTE_REQUERIDO);
		};
	}

	private String construirSolicitudRecepcion(String xml) {
		String xmlCodificado = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<soap:Envelope
				    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
				    <soap:Header/>
				    <soap:Body>
				        <ns2:validarComprobante
				            xmlns:ns2="http://ec.gob.sri.ws.recepcion">
				            <xml>%s</xml>
				        </ns2:validarComprobante>
				    </soap:Body>
				</soap:Envelope>
				""".formatted(xmlCodificado);
	}

	private String construirSolicitudAutorizacion(String claveAcceso) {
		String claveEscapada = escaparXml(claveAcceso);
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<soap:Envelope
				    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
				    <soap:Header/>
				    <soap:Body>
				        <ns2:autorizacionComprobante
				            xmlns:ns2="http://ec.gob.sri.ws.autorizacion">
				            <claveAccesoComprobante>%s</claveAccesoComprobante>
				        </ns2:autorizacionComprobante>
				    </soap:Body>
				</soap:Envelope>
				""".formatted(claveEscapada);
	}

	private String ejecutarSolicitud(String url, String soapRequest, String etapa) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "text/xml; charset=UTF-8").header("Accept", "text/xml")
					.POST(HttpRequest.BodyPublishers.ofString(soapRequest, StandardCharsets.UTF_8)).build();

			HttpResponse<String> response = httpClient.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			int statusCode = response.statusCode();
			String cuerpo = response.body();
			String contentType = response.headers().firstValue("Content-Type").orElse("no informado");

			if (statusCode >= 500) {
				registrarRespuestaInvalida(etapa, statusCode, contentType, messageResolver.resolver(MessageCodes.SRI_LOG_HTTP_SERVICIO_REMOTO), cuerpo, null);
				throw new SriCommunicationException(messageResolver.resolver(MessageCodes.SRI_HTTP_ERROR, statusCode));
			}

			if (statusCode < 200 || statusCode >= 300) {
				registrarRespuestaInvalida(etapa, statusCode, contentType, messageResolver.resolver(MessageCodes.SRI_LOG_HTTP_NO_EXITOSO), cuerpo, null);
				throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION);
			}

			if (cuerpo == null || cuerpo.isBlank()) {
				registrarRespuestaInvalida(etapa, statusCode, contentType, messageResolver.resolver(MessageCodes.SRI_LOG_RESPUESTA_VACIA), cuerpo, null);
				throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
			}

			if (esRespuestaHtml(cuerpo, contentType) || !pareceXml(cuerpo)) {
				registrarRespuestaInvalida(etapa, statusCode, contentType, messageResolver.resolver(MessageCodes.SRI_LOG_CONTENIDO_NO_SOAP), cuerpo, null);
				throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
			}

			return cuerpo;

		} catch (SriCommunicationException exception) {
			throw exception;
		} catch (ApplicationException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new SriCommunicationException(messageResolver.resolver(MessageCodes.SRI_ERROR_COMUNICACION), exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new SriCommunicationException(messageResolver.resolver(MessageCodes.SRI_ERROR_COMUNICACION), exception);
		}
	}

	private SriResponse procesarRespuestaRecepcion(String xml) {
		Document document = parsearXml(xml, "recepcion");
		validarEnvelopeSoap(document, "recepcion", xml);
		Element respuesta = obtenerPrimerElemento(document, "RespuestaRecepcionComprobante");
		if (respuesta == null) {
			registrarRespuestaInvalida("recepcion", null, null, messageResolver.resolver(MessageCodes.SRI_LOG_RESPUESTA_RECEPCION_AUSENTE), xml, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}
		if (obtenerTexto(respuesta, "estado") == null) {
			registrarRespuestaInvalida("recepcion", null, null, messageResolver.resolver(MessageCodes.SRI_LOG_ESTADO_RECEPCION_AUSENTE), xml, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}
		String estado = obtenerTexto(respuesta, "estado");
		List<SriMensaje> mensajes = obtenerMensajes(respuesta);
		String identificador = obtenerTexto(respuesta, "claveAcceso");
		return new SriResponse("RECIBIDA".equalsIgnoreCase(estado), estado, identificador, mensajes, xml);
	}

	private SriResponse procesarRespuestaAutorizacion(String xml) {
		Document document = parsearXml(xml, "autorizacion");
		validarEnvelopeSoap(document, "autorizacion", xml);
		Element respuesta = obtenerPrimerElemento(document, "RespuestaAutorizacionComprobante");
		if (respuesta == null) {
			registrarRespuestaInvalida("autorizacion", null, null, "no contiene RespuestaAutorizacionComprobante", xml, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}
		Element autorizacion = obtenerPrimerElemento(respuesta, "autorizacion");
		if (autorizacion == null) {
			return new SriResponse(false, null, obtenerTexto(respuesta, "claveAccesoConsultada"),
					obtenerMensajes(respuesta));
		}
		String estado = obtenerTexto(autorizacion, "estado");
		String numeroAutorizacion = obtenerTexto(autorizacion, "numeroAutorizacion");
		List<SriMensaje> mensajes = obtenerMensajes(autorizacion);
		return new SriResponse("AUTORIZADO".equalsIgnoreCase(estado), estado, numeroAutorizacion, mensajes, xml);
	}

	private List<SriMensaje> obtenerMensajes(Element contenedor) {
		List<SriMensaje> mensajes = new ArrayList<>();
		NodeList nodos = contenedor.getElementsByTagNameNS("*", "mensaje");
		for (int i = 0; i < nodos.getLength(); i++) {
			Node nodo = nodos.item(i);
			if (!(nodo instanceof Element mensaje)) {
				continue;
			}
			Node padre = mensaje.getParentNode();
			if (padre instanceof Element && "mensajes".equals(padre.getLocalName())) {
				mensajes.add(new SriMensaje(obtenerTexto(mensaje, "identificador"), obtenerTexto(mensaje, "mensaje"),
						obtenerTexto(mensaje, "informacionAdicional"), obtenerTexto(mensaje, "tipo")));
			}
		}
		return mensajes;
	}

	private Element obtenerPrimerElemento(Node nodo, String nombre) {
		if (nodo instanceof Element elemento && nombre.equals(elemento.getLocalName())) {
			return elemento;
		}
		NodeList nodos;
		if (nodo instanceof Document document) {
			nodos = document.getElementsByTagNameNS("*", nombre);
		} else if (nodo instanceof Element elemento) {
			nodos = elemento.getElementsByTagNameNS("*", nombre);
		} else {
			return null;
		}
		if (nodos.getLength() == 0) {
			return null;
		}
		return (Element) nodos.item(0);
	}

	private String obtenerTexto(Element elemento, String nombre) {
		Element hijo = obtenerPrimerElemento(elemento, nombre);
		if (hijo == null) {
			return null;
		}
		String texto = hijo.getTextContent();
		return texto == null ? null : texto.trim();
	}

	private Document parsearXml(String xml, String etapa) {
		try {
			var builder = documentBuilderFactory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception exception) {
			registrarRespuestaInvalida(etapa, null, null, messageResolver.resolver(MessageCodes.SRI_LOG_XML_MALFORMADO), xml, exception);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA, exception);
		}
	}

	private void validarEnvelopeSoap(Document document, String etapa, String respuesta) {
		Element envelope = document.getDocumentElement();
		if (envelope == null || !"Envelope".equals(envelope.getLocalName())
				|| !SOAP_ENV_NAMESPACE.equals(envelope.getNamespaceURI())) {
			registrarRespuestaInvalida(etapa, null, null, messageResolver.resolver(MessageCodes.SRI_LOG_ENVELOPE_INVALIDO), respuesta, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}

		Element body = obtenerPrimerElemento(envelope, "Body");
		if (body == null) {
			registrarRespuestaInvalida(etapa, null, null, messageResolver.resolver(MessageCodes.SRI_LOG_BODY_AUSENTE), respuesta, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}

		Element fault = obtenerPrimerElemento(body, "Fault");
		if (fault != null) {
			String codigo = obtenerTexto(fault, "faultcode");
			String mensaje = obtenerTexto(fault, "faultstring");
			registrarRespuestaInvalida(etapa, null, null,
					messageResolver.resolver(MessageCodes.SRI_LOG_SOAP_FAULT, codigo, mensaje),
					respuesta, null);
			throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
		}
	}

	private boolean esRespuestaHtml(String cuerpo, String contentType) {
		String tipo = contentType == null ? "" : contentType.toLowerCase();
		String inicio = cuerpo.stripLeading().toLowerCase();
		return tipo.contains("text/html") || inicio.startsWith("<html") || inicio.startsWith("<!doctype html");
	}

	private boolean pareceXml(String cuerpo) {
		String inicio = cuerpo.stripLeading();
		return inicio.startsWith("<?xml") || inicio.matches("(?s)^<(?:[A-Za-z_][\\w.-]*:)?Envelope(?:\\s|>).*");
	}

	private void registrarRespuestaInvalida(String etapa, Integer estadoHttp, String contentType, String motivo,
			String respuesta, Exception exception) {
		String respuestaCompleta = respuesta == null ? "" : respuesta;
		String mensajeLog = messageResolver.resolver(MessageCodes.SRI_LOG_RESPUESTA_INVALIDA, etapa,
				properties.getAmbiente(), estadoHttp, contentType, motivo, respuestaCompleta);
		if (exception == null) {
			LOGGER.error(mensajeLog);
		} else {
			LOGGER.error(mensajeLog, exception);
		}
	}

	private String escaparXml(String valor) {
		return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	private void configurarParserSeguro(DocumentBuilderFactory factory) {
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}
}