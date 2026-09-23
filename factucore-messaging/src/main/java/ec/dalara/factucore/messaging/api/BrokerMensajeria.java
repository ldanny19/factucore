package ec.dalara.factucore.messaging.api;

public interface BrokerMensajeria {

    void publicar(String destino, EventoMensaje evento);

    void registrar(ConsumidorMensajes consumidor);

    TipoBroker tipo();
}
