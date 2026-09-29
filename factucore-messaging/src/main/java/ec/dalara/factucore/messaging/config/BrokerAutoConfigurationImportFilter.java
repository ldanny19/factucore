package ec.dalara.factucore.messaging.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class BrokerAutoConfigurationImportFilter
        implements AutoConfigurationImportFilter, EnvironmentAware {

    private static final String ENABLED_PROPERTY = "factucore.messaging.enabled";
    private static final String BROKER_PROPERTY = "factucore.messaging.broker";
    private static final String KAFKA_BROKER = "KAFKA";

    private static final String RABBIT_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";
    private static final String RABBIT_HEALTH_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration";
    private static final String RABBIT_METRICS_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration";

    private static final String KAFKA_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";
    private static final String KAFKA_HEALTH_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.kafka.KafkaHealthContributorAutoConfiguration";
    private static final String KAFKA_METRICS_AUTO_CONFIGURATION =
            "org.springframework.boot.actuate.autoconfigure.metrics.kafka.KafkaMetricsAutoConfiguration";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);
        String broker = environment.getProperty(BROKER_PROPERTY, KAFKA_BROKER);

        boolean kafka = KAFKA_BROKER.equalsIgnoreCase(broker);

        boolean[] matches = new boolean[autoConfigurationClasses.length];

        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String autoConfigurationClass = autoConfigurationClasses[i];

            if (!enabled) {
                matches[i] = !RABBIT_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !RABBIT_HEALTH_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !RABBIT_METRICS_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !KAFKA_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !KAFKA_HEALTH_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !KAFKA_METRICS_AUTO_CONFIGURATION.equals(autoConfigurationClass);
            } else if (kafka) {
                matches[i] = !RABBIT_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !RABBIT_HEALTH_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !RABBIT_METRICS_AUTO_CONFIGURATION.equals(autoConfigurationClass);
            } else {
                matches[i] = !KAFKA_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !KAFKA_HEALTH_AUTO_CONFIGURATION.equals(autoConfigurationClass)
                        && !KAFKA_METRICS_AUTO_CONFIGURATION.equals(autoConfigurationClass);
            }
        }

        return matches;
    }
}
