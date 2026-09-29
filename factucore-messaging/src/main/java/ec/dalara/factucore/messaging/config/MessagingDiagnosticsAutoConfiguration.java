package ec.dalara.factucore.messaging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingDiagnosticsAutoConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MessagingDiagnosticsAutoConfiguration.class);

    @Bean
    ApplicationRunner messagingConfigurationLogger(MessagingProperties properties) {
        return args -> LOGGER.info(
                "FACTUCORE-MESSAGING: configuracion efectiva -> enabled={}, broker={}, kafkaBootstrap={}, rabbitAddresses={}",
                properties.isEnabled(),
                properties.getBroker(),
                properties.getKafka().getBootstrapServers(),
                properties.getRabbitmq().getAddresses());
    }
}
