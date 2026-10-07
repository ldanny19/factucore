package ec.dalara.factucore.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ec.dalara.factucore.infrastructure.adapter.messaging.AuditoriaPeticionMessagingProperties;

@Configuration
@EnableConfigurationProperties(AuditoriaPeticionMessagingProperties.class)
public class AuditoriaPeticionMessagingConfiguration {
}
