package ec.dalara.factucore.infrastructure.configuration.workflow;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.workflow.reproceso")
public class WorkflowReprocessProperties {
    private boolean habilitado = true;
    private long intervaloMs = 5000;
}
