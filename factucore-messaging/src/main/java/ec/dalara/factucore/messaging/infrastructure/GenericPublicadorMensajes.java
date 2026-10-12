package ec.dalara.factucore.messaging.infrastructure;

import java.util.List;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import ec.dalara.factucore.messaging.config.MessageCodes;
import ec.dalara.factucore.messaging.config.MessageResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericPublicadorMensajes implements PublicadorMensajes {

	private final List<BrokerMensajeria> brokers;
	private final MessagingProperties properties;
	private final MessageResolver messageResolver;

	@Override
	public void publicar(String destino, EventoMensaje evento) {
		if (destino == null || destino.isBlank()) {
			throw new IllegalArgumentException(messageResolver.resolver(MessageCodes.MENSAJERIA_DESTINO_REQUERIDO));
		}
		if (evento == null) {
			throw new IllegalArgumentException(messageResolver.resolver(MessageCodes.MENSAJERIA_EVENTO_REQUERIDO));
		}

		var configuracion = properties.getDestinos().get(destino);
		if (configuracion == null) {
			throw new IllegalStateException(messageResolver.resolver(MessageCodes.MENSAJERIA_DESTINO_NO_CONFIGURADO, destino));
		}

		var broker = brokers.stream().filter(b -> b.soporta(destino)).findFirst().orElseThrow(
				() -> new IllegalStateException(messageResolver.resolver(MessageCodes.MENSAJERIA_BROKER_NO_CONFIGURADO, destino)));

		broker.publicar(destino, evento);
	}
}
