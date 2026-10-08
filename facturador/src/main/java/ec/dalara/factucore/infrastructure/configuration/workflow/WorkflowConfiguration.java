package ec.dalara.factucore.infrastructure.configuration.workflow;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración transversal del workflow.
 *
 * <p>El grafo de facturación se encuentra externalizado en
 * {@code configuraciones/facturador/camel/facturacion.xml}; esta clase no
 * reproduce sus transiciones en Java. Aquí solo se habilitan las capacidades
 * de infraestructura que requiere el workflow, como la planificación del
 * reproceso.</p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(WorkflowReprocessProperties.class)
public class WorkflowConfiguration {
}
