package ec.dalara.factucore.messaging.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record EventoMensaje(
        String id, String tipo, String version, Instant fecha,
        String correlationId, JsonNode payload) {
    public EventoMensaje {
        id = id == null ? UUID.randomUUID().toString() : id;
        tipo = Objects.requireNonNull(tipo, "tipo");
        version = Objects.requireNonNull(version, "version");
        fecha = Objects.requireNonNullElseGet(fecha, Instant::now);
        correlationId = Objects.requireNonNullElseGet(correlationId, id);
        payload = Objects.requireNonNull(payload, "payload");
    }

    public static EventoMensaje crear(String tipo, String version,
            String correlationId, JsonNode payload) {
        return new EventoMensaje(null, tipo, version, Instant.now(), correlationId, payload);
    }
}
