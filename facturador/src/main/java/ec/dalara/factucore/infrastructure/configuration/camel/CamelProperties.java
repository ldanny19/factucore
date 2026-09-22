package ec.dalara.factucore.infrastructure.configuration.camel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.workflow")
public class CamelProperties {

    private String motor;

    private String version;

    private RutasProperties rutas = new RutasProperties();

    private ReprocesoProperties reproceso = new ReprocesoProperties();

    @Getter
    @Setter
    public static class RutasProperties {

        private String facturacion;
    }

    @Getter
    @Setter
    public static class ReprocesoProperties {

        private boolean habilitado;
    }
}