package ec.dalara.factucore.messaging.api;

import java.util.Set;

public interface ConsumidorMensajes {

    String destino();

    String grupo();

    default Set<String> tiposEvento() {
        return Set.of();
    }

    void consumir(EventoMensaje evento);
}
