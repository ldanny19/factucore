package ec.dalara.factucore.messaging.api;

public interface PublicadorMensajes {

    void publicar(String destino, EventoMensaje evento);
}
