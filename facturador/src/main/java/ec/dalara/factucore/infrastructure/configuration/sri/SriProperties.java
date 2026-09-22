package ec.dalara.factucore.infrastructure.configuration.sri;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "factucore.sri")
public class SriProperties {

    private AmbienteProperties pruebas = new AmbienteProperties();

    private AmbienteProperties produccion = new AmbienteProperties();

    private EnvioProperties envio = new EnvioProperties();

    private AutorizacionProperties autorizacion = new AutorizacionProperties();

    @Getter
    @Setter
    public static class AmbienteProperties {

        private String recepcionUrl;

        private String autorizacionUrl;
    }

    @Getter
    @Setter
    public static class EnvioProperties {

        private int maxIntentos;

        private long esperaMs;
    }

    @Getter
    @Setter
    public static class AutorizacionProperties {

        private long esperaMs;

        private int maxIntentos;
    }
}