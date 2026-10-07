package ec.dalara.factucore.messaging.api.auditoria;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato canónico del registro de auditoría de una petición HTTP.
 *
 * <p>El contrato representa el contenido funcional que viaja dentro de
 * {@code EventoMensaje}. No contiene metadatos propios del envoltorio del evento.</p>
 */
public record RegistroAuditoriaHttp(
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
