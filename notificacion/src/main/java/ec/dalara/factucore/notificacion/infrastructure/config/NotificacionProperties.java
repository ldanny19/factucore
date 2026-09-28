package ec.dalara.factucore.notificacion.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.notificacion")
public class NotificacionProperties {

    private Correo correo = new Correo();
    private Plantilla plantilla = new Plantilla();

    @Getter
    @Setter
    public static class Correo {
        private String remitente;
        private String asunto;
    }

    @Getter
    @Setter
    public static class Plantilla {
        private String ruta;
    }
}
