package ec.dalara.factucore.infrastructure.adapter.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.notificacion")
public class NotificacionMessagingProperties {

    private String topico = "factucore.notificacion";
    private String tipoEvento = "COMPROBANTE_AUTORIZADO";
    private String versionEvento = "1.0";
}
