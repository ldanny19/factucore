package ec.dalara.factucore.messaging.api;

public interface ConsumidorMensajes {

    String topico();

    String grupo();

    void consumir(EventoMensaje evento);
}
