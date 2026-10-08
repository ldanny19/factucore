package ec.dalara.factucore.messaging.infrastructure.rabbitmq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.config.MessagingProperties;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMessagingConfiguration {

	@Bean
	BrokerMensajeria rabbitBrokerMensajeria(MessagingProperties properties, ObjectMapper objectMapper) {
		return new RabbitBrokerMensajeria(properties, objectMapper);
	}
}
