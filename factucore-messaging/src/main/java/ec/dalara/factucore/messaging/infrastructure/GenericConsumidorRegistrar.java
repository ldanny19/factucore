package ec.dalara.factucore.messaging.infrastructure;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.config.MessageCodes;
import ec.dalara.factucore.messaging.config.MessageResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericConsumidorRegistrar implements InitializingBean {

	private final List<BrokerMensajeria> brokers;
	private final List<ConsumidorMensajes> consumidores;
	private final MessageResolver messageResolver;

	@Override
	public void afterPropertiesSet() {
		consumidores.forEach(consumidor -> brokers.stream().filter(broker -> broker.soporta(consumidor.destino()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						messageResolver.resolver(MessageCodes.MENSAJERIA_BROKER_NO_CONFIGURADO, consumidor.destino())))
				.registrar(consumidor));
	}
}
