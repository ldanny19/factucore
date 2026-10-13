package ec.dalara.factucore.notificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class NotificacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionApplication.class, args);
    }
}
