package ec.dalara.factucore.auditoria.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuditoriaMessagingProperties.class)
public class AuditoriaMessagingConfiguration {
}
