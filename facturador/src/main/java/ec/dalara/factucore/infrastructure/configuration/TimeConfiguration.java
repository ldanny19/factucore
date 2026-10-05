package ec.dalara.factucore.infrastructure.configuration;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfiguration {

	@Bean
	Clock factuCoreClock(@Value("${factucore.configuracion.zona-horaria:America/Guayaquil}") String zonaHoraria) {
		return Clock.system(ZoneId.of(zonaHoraria));
	}
}
