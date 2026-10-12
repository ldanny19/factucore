package ec.dalara.factucore.messaging.api;

public interface BrokerMensajeria {

	TipoBroker tipo();

	boolean soporta(String destino);

	void publicar(String destino, EventoMensaje evento);

	void registrar(ConsumidorMensajes consumidor);
}
