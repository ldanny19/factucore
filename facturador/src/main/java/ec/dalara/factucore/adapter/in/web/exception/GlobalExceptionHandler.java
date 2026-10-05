package ec.dalara.factucore.adapter.in.web.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.AdministracionResponse;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.shared.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String ENDPOINT_COMPROBANTES = "/api/v1/comprobantes";
    private static final DateTimeFormatter FECHA_ADMINISTRACION =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
        String idTransaccion = obtenerTexto(body, "idTransaccion");
        String fechaInicioTexto = obtenerTexto(body, "fechaInicio");

        if (esEndpointComprobantes(request)) {
            OffsetDateTime fechaInicio = convertirOffsetDateTime(fechaInicioTexto);

            ComprobanteGeneracionResponse respuesta = ComprobanteGeneracionResponse.builder()
                    .idTransaccion(idTransaccion)
                    .fechaInicio(fechaInicio)
                    .fechaFin(OffsetDateTime.now(factuCoreClock))
                    .exitoso(false)
                    .codigo(codigo)
                    .mensaje(mensaje)
                    .resultado(null)
                    .claveAcceso(null)
                    .numeroComprobante(null)
                    .tipoDocumento(null)
                    .estadoSri(null)
                    .archivoPdf(null)
                    .build();

            return ResponseEntity.status(estado).body(respuesta);
        }

        LocalDateTime fechaInicio = convertirLocalDateTime(fechaInicioTexto);

        AdministracionResponse<Object> respuesta = AdministracionResponse.builder()
                .idTransaccion(idTransaccion)
                .fechaInicio(fechaInicio)
                .fechaFin(LocalDateTime.now(factuCoreClock))
                .exitoso(false)
                .codigo(codigo)
                .mensaje(mensaje)
                .datos(null)
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

    private String obtenerTexto(JsonNode body, String campo) {
        JsonNode nodo = body.get(campo);
        return nodo == null || nodo.isNull() ? null : nodo.asText();
    }

    private LocalDateTime convertirLocalDateTime(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(valor, FECHA_ADMINISTRACION);
        } catch (Exception exception) {
            return null;
        }
    }

    private OffsetDateTime convertirOffsetDateTime(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(valor);
        } catch (Exception exception) {
            return null;
        }
    }
}
