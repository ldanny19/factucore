package ec.dalara.factucore.auditoria.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.auditoria.peticion")
public class AuditoriaMessagingProperties {

    private String topico = "factucore.auditoria.peticion";
    private String grupoConsumidor = "factucore-auditoria";
}
