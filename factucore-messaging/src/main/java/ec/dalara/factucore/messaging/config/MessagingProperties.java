package ec.dalara.factucore.messaging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.messaging")
public class MessagingProperties {

    private boolean enabled = true;
    private String broker = "KAFKA";
    private KafkaProperties kafka = new KafkaProperties();
    private RabbitmqProperties rabbitmq = new RabbitmqProperties();
    private RetryProperties retry = new RetryProperties();

    @Getter
    @Setter
    public static class KafkaProperties {
        private String bootstrapServers = "localhost:9092";
        private String securityProtocol = "PLAINTEXT";
        private String clientId = "factucore";
        private ProducerProperties producer = new ProducerProperties();
        private ConsumerProperties consumer = new ConsumerProperties();
    }

    @Getter
    @Setter
    public static class ProducerProperties {
        private String acks = "all";
    }

    @Getter
    @Setter
    public static class ConsumerProperties {
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
        private int concurrency = 1;
    }

    @Getter
    @Setter
    public static class RabbitmqProperties {
        private String addresses = "localhost:5672";
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";
        private String exchangeType = "topic";
        private String exchangePrefix = "factucore";
        private boolean durable = true;
        private int concurrency = 1;
    }

    @Getter
    @Setter
    public static class RetryProperties {
        private long intervaloMs = 5000;
        private long maxIntentos = 3;
        private boolean dlqHabilitada = true;
        private String sufijoDlq = ".DLQ";
    }
}
