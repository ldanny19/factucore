package ec.dalara.factucore.messaging.infrastructure;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessageResolver;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import ec.dalara.factucore.messaging.config.PropertiesMessageResolver;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessagingCoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(MessageResolver.class)
	MessageResolver messageResolver() {
		return new PropertiesMessageResolver();
	}

	@Bean
	@ConditionalOnMissingBean(PublicadorMensajes.class)
	PublicadorMensajes publicadorMensajes(List<BrokerMensajeria> brokers, MessagingProperties properties,
			MessageResolver messageResolver) {
		return new GenericPublicadorMensajes(brokers, properties, messageResolver);
	}

	@Bean
	@ConditionalOnMissingBean
	GenericConsumidorRegistrar consumidorRegistrar(List<BrokerMensajeria> brokers,
			List<ConsumidorMensajes> consumidores, MessageResolver messageResolver) {
		return new GenericConsumidorRegistrar(brokers, consumidores, messageResolver);
	}
}
