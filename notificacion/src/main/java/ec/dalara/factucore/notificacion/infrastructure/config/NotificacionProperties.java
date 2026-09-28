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
    private String topico = "factucore.notificacion.comprobante";

    public String getTopico() { return topico; }
    public void setTopico(String topico) { this.topico = topico; }

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
