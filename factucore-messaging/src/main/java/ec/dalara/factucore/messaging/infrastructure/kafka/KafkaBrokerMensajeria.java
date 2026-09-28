package ec.dalara.factucore.messaging.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.*;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;

@RequiredArgsConstructor
public class KafkaBrokerMensajeria implements BrokerMensajeria {
    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String,String> template;
    private final ConsumerFactory<String,String> consumerFactory;

    @Override public void publicar(String destino, EventoMensaje evento) {
        try { template.send(destino, evento.id(), objectMapper.writeValueAsString(evento)); }
        catch (Exception e) { throw new IllegalStateException("No fue posible publicar el mensaje", e); }
    }

    @Override public void registrar(ConsumidorMensajes consumidor) {
        var cp = new ContainerProperties(consumidor.topico());
        cp.setGroupId(consumidor.grupo());
        var container = new ConcurrentMessageListenerContainer<>(consumerFactory, cp);
        container.setConcurrency(properties.getKafka().getConsumer().getConcurrency());
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        var recoverer = new DeadLetterPublishingRecoverer(template,
            (record, exception) -> new TopicPartition(
                record.topic() + properties.getRetry().getSufijoDlq(), record.partition()));

        DefaultErrorHandler errorHandler;
        if (properties.getRetry().isDlqHabilitada()) {
            errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(
                    properties.getRetry().getIntervaloMs(),
                    Math.max(0, properties.getRetry().getMaxIntentos() - 1)));
        } else {
            org.springframework.kafka.listener.ConsumerRecordRecoverer noOp =
                    (record, exception) -> { };
            errorHandler = new DefaultErrorHandler(noOp);
        }

        container.setCommonErrorHandler(errorHandler);
        container.setupMessageListener((ConsumerRecord<String,String> record) -> {
            try { consumidor.consumir(objectMapper.readValue(record.value(), EventoMensaje.class)); }
            catch (Exception e) { throw new IllegalStateException("No fue posible procesar el mensaje", e); }
        });
        container.start();
    }

    @Override public TipoBroker tipo() { return TipoBroker.KAFKA; }

    public static ConsumerFactory<String,String> crearConsumerFactory(MessagingProperties p) {
        var c = new HashMap<String,Object>();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,p.getKafka().getBootstrapServers());
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,p.getKafka().getConsumer().getAutoOffsetReset());
        c.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,p.getKafka().getConsumer().isEnableAutoCommit());
        c.put(ConsumerConfig.CLIENT_ID_CONFIG,p.getKafka().getClientId());
        c.put("security.protocol",p.getKafka().getSecurityProtocol());
        return new DefaultKafkaConsumerFactory<>(c);
    }

    public static ProducerFactory<String,String> crearProducerFactory(MessagingProperties p) {
        var c = new HashMap<String,Object>();
        c.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,p.getKafka().getBootstrapServers());
        c.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        c.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        c.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG,p.getKafka().getProducer().getAcks());
        c.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG,p.getKafka().getClientId());
        c.put("security.protocol",p.getKafka().getSecurityProtocol());
        return new DefaultKafkaProducerFactory<>(c);
    }
}
