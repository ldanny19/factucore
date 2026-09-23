package ec.dalara.factucore.infrastructure.configuration.workflow;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(WorkflowReprocessProperties.class)
public class WorkflowConfiguration {
}
