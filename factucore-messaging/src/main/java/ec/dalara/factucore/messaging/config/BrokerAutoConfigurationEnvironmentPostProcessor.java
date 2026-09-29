package ec.dalara.factucore.messaging.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class BrokerAutoConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String BROKER_PROPERTY = "factucore.messaging.broker";
    private static final String ENABLED_PROPERTY = "factucore.messaging.enabled";
    private static final String AUTO_CONFIG_EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";
    private static final String KAFKA_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";
    private static final String RABBIT_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String broker = environment.getProperty(BROKER_PROPERTY, "KAFKA");
        boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);

        String exclusion;
        if (!enabled) {
            exclusion = KAFKA_AUTO_CONFIGURATION + "," + RABBIT_AUTO_CONFIGURATION;
        } else if ("RABBITMQ".equalsIgnoreCase(broker)) {
            exclusion = KAFKA_AUTO_CONFIGURATION;
        } else {
            exclusion = RABBIT_AUTO_CONFIGURATION;
        }

        String configuredExclusions = environment.getProperty(AUTO_CONFIG_EXCLUDE_PROPERTY, "");
        String exclusions = configuredExclusions.isBlank()
                ? exclusion
                : configuredExclusions + "," + exclusion;

        environment.getSystemProperties().put(AUTO_CONFIG_EXCLUDE_PROPERTY, exclusions);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
