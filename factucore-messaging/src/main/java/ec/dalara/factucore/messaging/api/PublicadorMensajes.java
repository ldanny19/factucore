package ec.dalara.factucore.messaging.api;

public interface PublicadorMensajes {

    void publicar(EventoMensaje evento);

    void publicar(String destino, EventoMensaje evento);
}
