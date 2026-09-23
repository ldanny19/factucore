package ec.dalara.factucore.messaging.infrastructure;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericPublicadorMensajes implements PublicadorMensajes {

    private final BrokerMensajeria broker;

    @Override
    public void publicar(String topico, EventoMensaje evento) {
        broker.publicar(topico, evento);
    }
}
