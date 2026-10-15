package ec.dalara.factucore.infrastructure.configuration.messaging;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factucore.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicInitializer implements ApplicationRunner {

	private static final Logger LOGGER = LogManager.getLogger(KafkaTopicInitializer.class);
	private static final int MAX_INTENTOS = 30;
	private static final long ESPERA_MS = 2_000L;
	private static final int PARTICIONES = 1;
	private static final short FACTOR_REPLICACION = 1;

	private final String bootstrapServers;
	private final List<String> topicos;

	public KafkaTopicInitializer(@Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}") String bootstrapServers,
			@Value("${factucore.messaging.destinos.auditoria.nombre:auditoria}") String topicoAuditoria,
			@Value("${factucore.messaging.destinos.notificacion.nombre:notificacion}") String topicoNotificacion) {
		this.bootstrapServers = bootstrapServers;
		this.topicos = List.of(topicoAuditoria, topicoNotificacion);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Map<String, Object> configuracion = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000, AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
				5_000);

		try (AdminClient adminClient = AdminClient.create(configuracion)) {
			inicializarTopicos(adminClient);
		}
	}

	private void inicializarTopicos(AdminClient adminClient) throws Exception {
		Exception ultimoError = null;

		for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
			try {
				for (String topico : topicos) {
					crearTopicoSiNoExiste(adminClient, topico);
				}

				LOGGER.info("Inicialización de topics Kafka completada: {}", topicos);
				return;
			} catch (Exception exception) {
				ultimoError = exception;
				if (intento == MAX_INTENTOS) {
					break;
				}

				LOGGER.warn("Kafka todavía no está disponible para inicializar los topics. Intento {}/{}.", intento,
						MAX_INTENTOS);
				Thread.sleep(ESPERA_MS);
			}
		}

		throw new IllegalStateException(
				"No fue posible inicializar los topics Kafka después de " + MAX_INTENTOS + " intentos.", ultimoError);
	}

	private void crearTopicoSiNoExiste(AdminClient adminClient, String nombreTopico) throws Exception {
		NewTopic topico = new NewTopic(nombreTopico, PARTICIONES, FACTOR_REPLICACION);

		try {
			adminClient.createTopics(List.of(topico)).values().get(nombreTopico).get(Duration.ofSeconds(5).toMillis(),
					TimeUnit.MILLISECONDS);
			LOGGER.info("Topic Kafka creado: {}", nombreTopico);
		} catch (ExecutionException exception) {
			if (exception.getCause() instanceof TopicExistsException) {
				LOGGER.debug("El topic Kafka ya existe: {}", nombreTopico);
				return;
			}

			throw exception;
		}
	}
}
