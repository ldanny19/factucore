package ec.dalara.factucore.adapter.in.web.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.shared.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String ENDPOINT_COMPROBANTES = "/api/v1/comprobantes";

    private final MessageResolver messageResolver;
    private final ObjectMapper objectMapper;
    private final Clock factuCoreClock;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<?> manejarDomainException(
            DomainException exception,
            HttpServletRequest request) {

        return construirRespuesta(
                exception.getCodigo(),
                exception.getParametros(),
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> manejarApplicationException(
            ApplicationException exception,
            HttpServletRequest request) {

        return construirRespuesta(
                exception.getCodigo(),
                exception.getParametros(),
                HttpStatus.BAD_REQUEST,
                request);
    }

    private ResponseEntity<?> construirRespuesta(
            String codigo,
            Object[] parametros,
            HttpStatus estado,
            HttpServletRequest request) {

        String mensaje = messageResolver.resolver(
                codigo,
                Locale.getDefault(),
                parametros);

        JsonNode body = obtenerBody(request);

        String idTransaccion = texto(body, "idTransaccion");

        if (esEndpointComprobantes(request)) {
            return construirRespuestaComprobante(
                    body, idTransaccion, codigo, mensaje, estado);
        }

        return construirRespuestaAdministracion(
                body, idTransaccion, codigo, mensaje, estado);
    }

    private ResponseEntity<ComprobanteGeneracionResponse> construirRespuestaComprobante(
            JsonNode body,
            String idTransaccion,
            String codigo,
            String mensaje,
            HttpStatus estado) {

        ComprobanteGeneracionResponse respuesta =
                ComprobanteGeneracionResponse.builder()
                        .idTransaccion(idTransaccion)
                        .fechaInicio(fechaOffset(body, "fechaInicio"))
                        .fechaFin(OffsetDateTime.now(factuCoreClock))
                        .exitoso(false)
                        .codigo(codigo)
                        .mensaje(mensaje)
                        .resultado(null)
                        .claveAcceso(null)
                        .numeroComprobante(null)
                        .tipoDocumento(texto(body, "tipoDocumento"))
                        .estadoSri(null)
                        .archivoPdf(null)
                        .build();

        return ResponseEntity.status(estado).body(respuesta);
    }

    private ResponseEntity<AdministracionResponse<JsonNode>> construirRespuestaAdministracion(
            JsonNode body,
            String idTransaccion,
            String codigo,
            String mensaje,
            HttpStatus estado) {

        AdministracionResponse<JsonNode> respuesta =
                AdministracionResponse.<JsonNode>builder()
                        .idTransaccion(idTransaccion)
                        .fechaInicio(fechaLocal(body, "fechaInicio"))
                        .fechaFin(LocalDateTime.now(factuCoreClock))
                        .exitoso(false)
                        .codigo(codigo)
                        .mensaje(mensaje)
                        .datos(body.get("datos"))
                        .build();

        return ResponseEntity.status(estado).body(respuesta);
    }

    private boolean esEndpointComprobantes(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return ENDPOINT_COMPROBANTES.equals(uri)
                || (uri != null && uri.equals(ENDPOINT_COMPROBANTES + "/"));
    }

    private JsonNode obtenerBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return objectMapper.createObjectNode();
        }

        byte[] contenido = wrapper.getContentAsByteArray();

        if (contenido.length == 0) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(contenido);
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String texto(JsonNode body, String campo) {
        JsonNode nodo = body.get(campo);
        return nodo == null || nodo.isNull() ? null : nodo.asText();
    }

    private LocalDateTime fechaLocal(JsonNode body, String campo) {
        JsonNode nodo = body.get(campo);
        if (nodo == null || nodo.isNull()) {
            return null;
        }

        try {
            return objectMapper.treeToValue(nodo, LocalDateTime.class);
        } catch (Exception exception) {
            return null;
        }
    }

    private OffsetDateTime fechaOffset(JsonNode body, String campo) {
        JsonNode nodo = body.get(campo);
        if (nodo == null || nodo.isNull()) {
            return null;
        }

        try {
            return objectMapper.treeToValue(nodo, OffsetDateTime.class);
        } catch (Exception exception) {
            return null;
        }
    }
}
