package ec.dalara.factucore.messaging.api;

@FunctionalInterface
public interface MensajeHandler {
    void aceptar(EventoMensaje evento);
}
