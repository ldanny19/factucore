package ec.dalara.factucore.infrastructure.configuration.camel;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CamelProperties.class)
public class CamelConfiguration {
}