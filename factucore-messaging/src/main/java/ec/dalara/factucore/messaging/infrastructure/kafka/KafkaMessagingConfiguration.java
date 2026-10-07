package ec.dalara.factucore.messaging.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import ec.dalara.factucore.messaging.infrastructure.GenericConsumidorRegistrar;
import ec.dalara.factucore.messaging.infrastructure.GenericPublicadorMensajes;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaMessagingConfiguration {

    @Bean
    BrokerMensajeria kafkaBrokerMensajeria(
            MessagingProperties properties,
            ObjectMapper objectMapper) {
        return new KafkaBrokerMensajeria(properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(PublicadorMensajes.class)
    PublicadorMensajes publicadorMensajes(
            java.util.List<BrokerMensajeria> brokers,
            MessagingProperties properties) {
        return new GenericPublicadorMensajes(brokers, properties);
    }

    @Bean
    GenericConsumidorRegistrar consumidorRegistrar(
            java.util.List<BrokerMensajeria> brokers,
            java.util.List<ec.dalara.factucore.messaging.api.ConsumidorMensajes> consumidores) {
        return new GenericConsumidorRegistrar(brokers, consumidores);
    }
}
