package ec.dalara.factucore.messaging.infrastructure.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import ec.dalara.factucore.messaging.infrastructure.GenericConsumidorRegistrar;
import ec.dalara.factucore.messaging.infrastructure.GenericPublicadorMensajes;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "broker", havingValue = "RABBITMQ")
public class RabbitMessagingConfiguration {

    @Bean
    ConnectionFactory rabbitConnectionFactory(MessagingProperties properties) {
        var factory = new CachingConnectionFactory();
        factory.setAddresses(properties.getRabbitmq().getAddresses());
        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        factory.setVirtualHost(properties.getRabbitmq().getVirtualHost());
        return factory;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    BrokerMensajeria rabbitBrokerMensajeria(
            MessagingProperties properties,
            ObjectMapper objectMapper,
            RabbitTemplate template,
            ConnectionFactory connectionFactory,
            RabbitAdmin admin) {
        return new RabbitBrokerMensajeria(
                properties, objectMapper, template, connectionFactory, admin);
    }

    @Bean
    @ConditionalOnMissingBean(PublicadorMensajes.class)
    PublicadorMensajes publicadorMensajes(BrokerMensajeria broker) {
        return new GenericPublicadorMensajes(broker);
    }

    @Bean
    GenericConsumidorRegistrar consumidorRegistrar(
            BrokerMensajeria broker,
            java.util.List<ec.dalara.factucore.messaging.api.ConsumidorMensajes> consumidores) {
        return new GenericConsumidorRegistrar(broker, consumidores);
    }
}
