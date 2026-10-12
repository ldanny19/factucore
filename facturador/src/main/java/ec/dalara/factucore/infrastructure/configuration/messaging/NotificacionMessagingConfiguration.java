package ec.dalara.factucore.infrastructure.configuration.messaging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ec.dalara.factucore.infrastructure.adapter.messaging.NotificacionMessagingProperties;

@Configuration
@EnableConfigurationProperties(NotificacionMessagingProperties.class)
public class NotificacionMessagingConfiguration {
}
