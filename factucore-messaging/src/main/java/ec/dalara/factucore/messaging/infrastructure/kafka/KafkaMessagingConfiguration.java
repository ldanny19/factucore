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
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "factucore.messaging", name = "broker", havingValue = "KAFKA", matchIfMissing = true)
public class KafkaMessagingConfiguration {

    @Bean
    ProducerFactory<String, String> kafkaProducerFactory(MessagingProperties properties) {
        return KafkaBrokerMensajeria.crearProducerFactory(properties);
    }

    @Bean
    KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ConsumerFactory<String, String> kafkaConsumerFactory(MessagingProperties properties) {
        return KafkaBrokerMensajeria.crearConsumerFactory(properties);
    }

    @Bean
    BrokerMensajeria kafkaBrokerMensajeria(
            MessagingProperties properties,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> template,
            ConsumerFactory<String, String> consumerFactory) {
        return new KafkaBrokerMensajeria(properties, objectMapper, template, consumerFactory);
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
