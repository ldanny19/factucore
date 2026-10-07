package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GenericPublicadorMensajes implements PublicadorMensajes {

    private final List<BrokerMensajeria> brokers;
    private final MessagingProperties properties;

    @Override
    public void publicar(String destino, EventoMensaje evento) {
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("El destino es requerido");
        }
        if (evento == null) {
            throw new IllegalArgumentException("El evento es requerido");
        }

        var configuracion = properties.getDestinos().get(destino);
        if (configuracion == null) {
            throw new IllegalStateException(
                    "No existe el destino de mensajería configurado: " + destino);
        }

        var broker = brokers.stream()
                .filter(b -> b.soporta(destino))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un broker configurado para el destino: " + destino));

        broker.publicar(destino, evento);
    }
}
