package ec.dalara.factucore.messaging.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ConditionalOnProperty(
        prefix = "factucore.messaging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class KafkaMessagingConfiguration {

    @Bean
    ProducerFactory<String, EventoMensaje> productorKafkaFactory(MessagingProperties properties) {
        var config = new java.util.HashMap<String, Object>();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG,
                properties.getProducer().getAcks());
        config.put("security.protocol", properties.getSecurityProtocol());
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    KafkaTemplate<String, EventoMensaje> kafkaTemplate(
            ProducerFactory<String, EventoMensaje> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ConsumerFactory<String, EventoMensaje> consumidorKafkaFactory(
            MessagingProperties properties,
            ObjectMapper objectMapper
    ) {
        var config = new java.util.HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                properties.getConsumer().getAutoOffsetReset());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                properties.getConsumer().isEnableAutoCommit());
        config.put("security.protocol", properties.getSecurityProtocol());

        var deserializer = new JsonDeserializer<>(EventoMensaje.class, objectMapper, false);
        deserializer.addTrustedPackages("ec.dalara.factucore.messaging.api");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, EventoMensaje> kafkaListenerContainerFactory(
            ConsumerFactory<String, EventoMensaje> consumerFactory,
            ProducerFactory<String, EventoMensaje> producerFactory,
            MessagingProperties properties
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, EventoMensaje>();
        factory.setConsumerFactory(consumerFactory);

        var recoverer = new DeadLetterPublishingRecoverer(
                new KafkaTemplate<>(producerFactory),
                (record, exception) ->
                        new org.apache.kafka.common.TopicPartition(
                                record.topic() + properties.getRetry().getSufijoDlq(),
                                record.partition()
                        )
        );

        factory.setCommonErrorHandler(new DefaultErrorHandler(
                properties.getRetry().isDlqHabilitada()
                        ? recoverer
                        : (record, exception) -> { },
                new FixedBackOff(
                        properties.getRetry().getIntervaloMs(),
                        Math.max(0, properties.getRetry().getMaxIntentos() - 1)
                )
        ));

        return factory;
    }
}
