package ec.dalara.factucore.messaging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class BrokerAutoConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(BrokerAutoConfigurationEnvironmentPostProcessor.class);

	private static final String ENABLED_PROPERTY = "factucore.messaging.enabled";
	private static final String BROKER_PROPERTY = "factucore.messaging.broker";
	private static final String EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";
	private static final String ACTUATOR_INCLUDE_PROPERTY = "management.endpoints.web.exposure.include";
	private static final String ACTUATOR_EXCLUDE_PROPERTY = "management.endpoints.web.exposure.exclude";
	private static final String RABBIT_HEALTH_PROPERTY = "management.health.rabbit.enabled";
	private static final String KAFKA_HEALTH_PROPERTY = "management.health.kafka.enabled";

	private static final String RABBIT_AUTO_CONFIGURATION = "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";
	private static final String RABBIT_HEALTH_AUTO_CONFIGURATION = "org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration";
	private static final String RABBIT_METRICS_AUTO_CONFIGURATION = "org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration";

	private static final String KAFKA_AUTO_CONFIGURATION = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";
	private static final String KAFKA_HEALTH_AUTO_CONFIGURATION = "org.springframework.boot.actuate.autoconfigure.kafka.KafkaHealthContributorAutoConfiguration";
	private static final String KAFKA_METRICS_AUTO_CONFIGURATION = "org.springframework.boot.actuate.autoconfigure.metrics.kafka.KafkaMetricsAutoConfiguration";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

		boolean enabled = environment.getProperty(ENABLED_PROPERTY, Boolean.class, true);
		String broker = environment.getProperty(BROKER_PROPERTY, "KAFKA");

		boolean kafka = "KAFKA".equalsIgnoreCase(broker);

		String[] exclusions;

		if (!enabled) {
			exclusions = new String[] { RABBIT_AUTO_CONFIGURATION, RABBIT_HEALTH_AUTO_CONFIGURATION,
					RABBIT_METRICS_AUTO_CONFIGURATION, KAFKA_AUTO_CONFIGURATION, KAFKA_HEALTH_AUTO_CONFIGURATION,
					KAFKA_METRICS_AUTO_CONFIGURATION };
		} else if (kafka) {
			exclusions = new String[] { RABBIT_AUTO_CONFIGURATION, RABBIT_HEALTH_AUTO_CONFIGURATION,
					RABBIT_METRICS_AUTO_CONFIGURATION };
		} else {
			exclusions = new String[] { KAFKA_AUTO_CONFIGURATION, KAFKA_HEALTH_AUTO_CONFIGURATION,
					KAFKA_METRICS_AUTO_CONFIGURATION };
		}

		String configuredExclusions = environment.getProperty(EXCLUDE_PROPERTY, "");

		String exclusionsValue = String.join(",", exclusions);

		if (configuredExclusions.isBlank()) {
			environment.getSystemProperties().put(EXCLUDE_PROPERTY, exclusionsValue);
		} else {
			environment.getSystemProperties().put(EXCLUDE_PROPERTY, configuredExclusions + "," + exclusionsValue);
		}

		if (enabled) {
			environment.getSystemProperties().put(ACTUATOR_INCLUDE_PROPERTY, "health,info");
			environment.getSystemProperties().put(ACTUATOR_EXCLUDE_PROPERTY, "");
			environment.getSystemProperties().put(RABBIT_HEALTH_PROPERTY, Boolean.toString(!kafka));
			environment.getSystemProperties().put(KAFKA_HEALTH_PROPERTY, Boolean.toString(kafka));
		} else {
			environment.getSystemProperties().put(ACTUATOR_INCLUDE_PROPERTY, "");
			environment.getSystemProperties().put(ACTUATOR_EXCLUDE_PROPERTY, "*");
			environment.getSystemProperties().put(RABBIT_HEALTH_PROPERTY, "false");
			environment.getSystemProperties().put(KAFKA_HEALTH_PROPERTY, "false");
		}

		LOGGER.info("FACTUCORE-MESSAGING: enabled={}, broker={}, auto-configuracion excluida={}", enabled, broker,
				exclusionsValue);

		LOGGER.info("FACTUCORE-MESSAGING: actuator={}, health/info expuestos={}", enabled,
				enabled ? "health,info" : "ninguno");
	}
}
