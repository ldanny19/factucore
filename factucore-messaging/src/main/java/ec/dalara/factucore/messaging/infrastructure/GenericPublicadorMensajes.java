package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GenericPublicadorMensajes implements PublicadorMensajes {

    private final BrokerMensajeria broker;
    private final MessagingProperties properties;

    @Override
    public void publicar(EventoMensaje evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento es requerido");
        }

        List<String> destinos = properties.getRutas().getOrDefault(evento.tipo(), List.of())
                .stream()
                .filter(destino -> destino != null && !destino.isBlank())
                .distinct()
                .toList();

        if (destinos.isEmpty()) {
            throw new IllegalStateException(
                    "No existe una ruta de mensajería configurada para el tipo de evento: " + evento.tipo());
        }

        destinos.forEach(destino -> broker.publicar(destino, evento));
    }

    @Override
    public void publicar(String destino, EventoMensaje evento) {
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("El destino es requerido");
        }
        if (evento == null) {
            throw new IllegalArgumentException("El evento es requerido");
        }
        broker.publicar(destino, evento);
    }
}
