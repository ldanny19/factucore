package ec.dalara.factucore.notificacion.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(NotificacionProperties.class)
@EnableScheduling
public class NotificacionConfiguration {
}
