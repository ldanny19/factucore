package ec.dalara.factucore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FacturadorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacturadorApplication.class, args);
	}
}