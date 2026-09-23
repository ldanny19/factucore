package ec.dalara.factucore.adapter.out.sri;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.SriPort;
import ec.dalara.factucore.application.port.out.sri.SriCommunicationException;
import ec.dalara.factucore.application.port.out.sri.SriMensaje;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;

@Component
public class SriSoapAdapter implements SriPort {

    private static final String SOAP_ENV_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String RECEPCION_NAMESPACE = "http://ec.gob.sri.ws.recepcion";
    private static final String AUTORIZACION_NAMESPACE = "http://ec.gob.sri.ws.autorizacion";

    private final SriProperties properties;
    private final HttpClient httpClient;
    private final DocumentBuilderFactory documentBuilderFactory;

    public SriSoapAdapter(SriProperties properties) {
        this.properties = properties;
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
        String respuesta = ejecutarSolicitud(url, soapRequest);
        return procesarRespuestaRecepcion(respuesta);
    }

    @Override
    public SriResponse autorizar(String claveAcceso) {
        if (claveAcceso == null || claveAcceso.isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
        }
        String url = obtenerUrlAutorizacion();
        String soapRequest = construirSolicitudAutorizacion(claveAcceso);
        String respuesta = ejecutarSolicitud(url, soapRequest);
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
        String xmlCodificado = escaparXml(xml);
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

    private String ejecutarSolicitud(String url, String soapRequest) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "text/xml; charset=UTF-8")
                    .header("Accept", "text/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(soapRequest, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int statusCode = response.statusCode();

            if (statusCode >= 500) {
                throw new SriCommunicationException("SRI respondió con estado HTTP " + statusCode);
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION);
            }

            if (response.body() == null || response.body().isBlank()) {
                throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
            }

            return response.body();

        } catch (SriCommunicationException exception) {
            throw exception;
        } catch (ApplicationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new SriCommunicationException("Error de comunicación con el SRI", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SriCommunicationException("Comunicación con el SRI interrumpida", exception);
        }
    }

    private SriResponse procesarRespuestaRecepcion(String xml) {
        Document document = parsearXml(xml);
        Element respuesta = obtenerPrimerElemento(document, "RespuestaRecepcionComprobante");
        if (respuesta == null) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }
        String estado = obtenerTexto(respuesta, "estado");
        List<SriMensaje> mensajes = obtenerMensajes(respuesta, "comprobante");
        String identificador = obtenerTexto(respuesta, "claveAcceso");
        return new SriResponse("RECIBIDA".equalsIgnoreCase(estado), estado, identificador, mensajes);
    }

    private SriResponse procesarRespuestaAutorizacion(String xml) {
        Document document = parsearXml(xml);
        Element respuesta = obtenerPrimerElemento(document, "RespuestaAutorizacionComprobante");
        if (respuesta == null) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }
        Element autorizacion = obtenerPrimerElemento(respuesta, "autorizacion");
        if (autorizacion == null) {
            return new SriResponse(false, null, obtenerTexto(respuesta, "claveAccesoConsultada"),
                    obtenerMensajes(respuesta, "autorizacion"));
        }
        String estado = obtenerTexto(autorizacion, "estado");
        String numeroAutorizacion = obtenerTexto(autorizacion, "numeroAutorizacion");
        List<SriMensaje> mensajes = obtenerMensajes(autorizacion, "autorizacion");
        return new SriResponse("AUTORIZADO".equalsIgnoreCase(estado), estado, numeroAutorizacion, mensajes);
    }

    private List<SriMensaje> obtenerMensajes(Element contenedor, String elementoContenedor) {
        List<SriMensaje> mensajes = new ArrayList<>();
        NodeList nodos = contenedor.getElementsByTagNameNS("*", "mensaje");
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (!(nodo instanceof Element mensaje)) {
                continue;
            }
            Node padre = mensaje.getParentNode();
            if (padre instanceof Element && "mensajes".equals(padre.getLocalName())) {
                mensajes.add(new SriMensaje(
                        obtenerTexto(mensaje, "identificador"),
                        obtenerTexto(mensaje, "mensaje"),
                        obtenerTexto(mensaje, "informacionAdicional"),
                        obtenerTexto(mensaje, "tipo")));
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

    private Document parsearXml(String xml) {
        try {
            var builder = documentBuilderFactory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception exception) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }
    }

    private String escaparXml(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace(""", "&quot;").replace("'", "&apos;");
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