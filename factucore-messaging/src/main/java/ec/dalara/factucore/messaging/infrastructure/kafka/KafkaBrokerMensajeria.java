package ec.dalara.factucore.messaging.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.TipoBroker;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;

@RequiredArgsConstructor
public class KafkaBrokerMensajeria implements BrokerMensajeria {

    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;
    private final ConsumerFactory<String, String> consumerFactory;

    @Override
    public void publicar(String destino, EventoMensaje evento) {
        try {
            template.send(destino, evento.id(), objectMapper.writeValueAsString(evento));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible publicar el mensaje", e);
        }
    }

    @Override
    public void registrar(ConsumidorMensajes consumidor) {
        var containerProperties = new ContainerProperties(consumidor.topico());
        containerProperties.setGroupId(consumidor.grupo());

        var container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setConcurrency(properties.getKafka().getConsumer().getConcurrency());
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        var recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, exception) -> new TopicPartition(
                        record.topic() + properties.getRetry().getSufijoDlq(),
                        record.partition()
                )
        );

        container.setCommonErrorHandler(new DefaultErrorHandler(
                properties.getRetry().isDlqHabilitada()
                        ? recoverer
                        : (record, exception) -> { },
                new FixedBackOff(
                        properties.getRetry().getIntervaloMs(),
                        Math.max(0, properties.getRetry().getMaxIntentos() - 1)
                )
        ));

        container.setupMessageListener((ConsumerRecord<String, String> record) -> {
            try {
                consumidor.consumir(objectMapper.readValue(record.value(), EventoMensaje.class));
            } catch (Exception e) {
                throw new IllegalStateException("No fue posible procesar el mensaje", e);
            }
        });
        container.start();
    }

    @Override
    public TipoBroker tipo() {
        return TipoBroker.KAFKA;
    }

    public static ConsumerFactory<String, String> crearConsumerFactory(MessagingProperties properties) {
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getKafka().getConsumer().getAutoOffsetReset());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.getKafka().getConsumer().isEnableAutoCommit());
        config.put("security.protocol", properties.getKafka().getSecurityProtocol());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    public static org.springframework.kafka.core.ProducerFactory<String, String> crearProducerFactory(MessagingProperties properties) {
        var config = new HashMap<String, Object>();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, properties.getKafka().getProducer().getAcks());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, properties.getKafka().getClientId());
        config.put("security.protocol", properties.getKafka().getSecurityProtocol());
        return new DefaultKafkaProducerFactory<>(config);
    }
}
