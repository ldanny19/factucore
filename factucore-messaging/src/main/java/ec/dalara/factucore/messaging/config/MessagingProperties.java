package ec.dalara.factucore.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factucore.messaging")
public class MessagingProperties {

    private boolean enabled = true;
    private String broker = "KAFKA";
    private KafkaProperties kafka = new KafkaProperties();
    private RabbitmqProperties rabbitmq = new RabbitmqProperties();
    private RetryProperties retry = new RetryProperties();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }
    public KafkaProperties getKafka() { return kafka; }
    public void setKafka(KafkaProperties kafka) { this.kafka = kafka; }
    public RabbitmqProperties getRabbitmq() { return rabbitmq; }
    public void setRabbitmq(RabbitmqProperties rabbitmq) { this.rabbitmq = rabbitmq; }
    public RetryProperties getRetry() { return retry; }
    public void setRetry(RetryProperties retry) { this.retry = retry; }

    public static class KafkaProperties {
        private String bootstrapServers = "localhost:9092";
        private String securityProtocol = "PLAINTEXT";
        private String clientId = "factucore";
        private ProducerProperties producer = new ProducerProperties();
        private ConsumerProperties consumer = new ConsumerProperties();

        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String v) { bootstrapServers = v; }
        public String getSecurityProtocol() { return securityProtocol; }
        public void setSecurityProtocol(String v) { securityProtocol = v; }
        public String getClientId() { return clientId; }
        public void setClientId(String v) { clientId = v; }
        public ProducerProperties getProducer() { return producer; }
        public void setProducer(ProducerProperties v) { producer = v; }
        public ConsumerProperties getConsumer() { return consumer; }
        public void setConsumer(ConsumerProperties v) { consumer = v; }
    }

    public static class ProducerProperties {
        private String acks = "all";
        public String getAcks() { return acks; }
        public void setAcks(String v) { acks = v; }
    }

    public static class ConsumerProperties {
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
        private int concurrency = 1;
        public String getAutoOffsetReset() { return autoOffsetReset; }
        public void setAutoOffsetReset(String v) { autoOffsetReset = v; }
        public boolean isEnableAutoCommit() { return enableAutoCommit; }
        public void setEnableAutoCommit(boolean v) { enableAutoCommit = v; }
        public int getConcurrency() { return concurrency; }
        public void setConcurrency(int v) { concurrency = v; }
    }

    public static class RabbitmqProperties {
        private String addresses = "localhost:5672";
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";
        private String exchangeType = "topic";
        private String exchangePrefix = "factucore";
        private boolean durable = true;
        private int concurrency = 1;
        public String getAddresses() { return addresses; }
        public void setAddresses(String v) { addresses = v; }
        public String getUsername() { return username; }
        public void setUsername(String v) { username = v; }
        public String getPassword() { return password; }
        public void setPassword(String v) { password = v; }
        public String getVirtualHost() { return virtualHost; }
        public void setVirtualHost(String v) { virtualHost = v; }
        public String getExchangeType() { return exchangeType; }
        public void setExchangeType(String v) { exchangeType = v; }
        public String getExchangePrefix() { return exchangePrefix; }
        public void setExchangePrefix(String v) { exchangePrefix = v; }
        public boolean isDurable() { return durable; }
        public void setDurable(boolean v) { durable = v; }
        public int getConcurrency() { return concurrency; }
        public void setConcurrency(int v) { concurrency = v; }
    }

    public static class RetryProperties {
        private long intervaloMs = 5000;
        private long maxIntentos = 3;
        private boolean dlqHabilitada = true;
        private String sufijoDlq = ".DLQ";
        public long getIntervaloMs() { return intervaloMs; }
        public void setIntervaloMs(long v) { intervaloMs = v; }
        public long getMaxIntentos() { return maxIntentos; }
        public void setMaxIntentos(long v) { maxIntentos = v; }
        public boolean isDlqHabilitada() { return dlqHabilitada; }
        public void setDlqHabilitada(boolean v) { dlqHabilitada = v; }
        public String getSufijoDlq() { return sufijoDlq; }
        public void setSufijoDlq(String v) { sufijoDlq = v; }
    }
}
