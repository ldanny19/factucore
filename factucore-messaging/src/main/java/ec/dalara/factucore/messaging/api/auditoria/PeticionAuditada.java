package ec.dalara.factucore.messaging.api.auditoria;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato canónico del evento de auditoría de una petición HTTP.
 *
 * <p>Este contrato es compartido entre los productores de eventos
 * (Facturador y futuros servicios) y el microservicio Auditoría.</p>
 */
public record PeticionAuditada(
        String idTransaccion,
        Long idEmpresa,
        String canal,
        String metodoHttp,
        String endpoint,
        String uriSolicitada,
        String usuario,
        String ipOrigen,
        Instant fechaInicio,
        Instant fechaFin,
        Long duracionMs,
        Integer estadoHttp,
        Map<String, String> peticionHeaders,
        String peticionBody,
        Map<String, String> respuestaHeaders,
        String respuestaBody
) {
}
