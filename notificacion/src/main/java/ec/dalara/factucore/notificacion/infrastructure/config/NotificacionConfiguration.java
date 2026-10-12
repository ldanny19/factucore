package ec.dalara.factucore.notificacion.infrastructure.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties; import org.springframework.context.annotation.Configuration;
@Configuration @EnableConfigurationProperties(NotificacionProperties.class) public class NotificacionConfiguration {}