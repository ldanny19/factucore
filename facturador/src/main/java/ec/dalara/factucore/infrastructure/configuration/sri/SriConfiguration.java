package ec.dalara.factucore.infrastructure.configuration.sri;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SriProperties.class)
public class SriConfiguration {
}