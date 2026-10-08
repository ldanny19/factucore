package ec.dalara.factucore.messaging.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.backoff.FixedBackOff;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.TipoBroker;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaBrokerMensajeria implements BrokerMensajeria {

	private final MessagingProperties properties;
	private final ObjectMapper objectMapper;
	private final Map<String, KafkaTemplate<String, String>> templates = new ConcurrentHashMap<>();
	private final Map<String, ConsumerFactory<String, String>> consumerFactories = new ConcurrentHashMap<>();

	@Override
	public TipoBroker tipo() {
		return TipoBroker.KAFKA;
	}

	@Override
	public boolean soporta(String destino) {
		var config = properties.getDestinos().get(destino);
		if (config == null)
			return false;
		var conexion = properties.getConexiones().get(config.getConexion());
		return conexion != null && conexion.getTipo() == TipoBroker.KAFKA;
	}

	@Override
	public void publicar(String destino, EventoMensaje evento) {
		try {
			var configuracion = destino(destino);
			template(configuracion.getConexion()).send(configuracion.getNombre(), evento.id(),
					objectMapper.writeValueAsString(evento));
		} catch (Exception e) {
			throw new IllegalStateException("No fue posible publicar el mensaje", e);
		}
	}

	@Override
	public void registrar(ConsumidorMensajes consumidor) {
		var configuracion = destino(consumidor.destino());
		var factory = consumerFactory(configuracion.getConexion());
		var cp = new ContainerProperties(configuracion.getNombre());
		cp.setGroupId(consumidor.grupo());

		var container = new ConcurrentMessageListenerContainer<>(factory, cp);
		container.setConcurrency(conexion(configuracion.getConexion()).getKafka().getConsumer().getConcurrency());
		container.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

		var template = template(configuracion.getConexion());
		BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinoDlq = (record,
				exception) -> new TopicPartition(record.topic() + properties.getRetry().getSufijoDlq(),
						record.partition());
		var recoverer = new DeadLetterPublishingRecoverer(template, destinoDlq);

		DefaultErrorHandler errorHandler;
		if (properties.getRetry().isDlqHabilitada()) {
			errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(properties.getRetry().getIntervaloMs(),
					Math.max(0, properties.getRetry().getMaxIntentos() - 1)));
		} else {
			errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0L));
		}

		container.setCommonErrorHandler(errorHandler);
		container.setupMessageListener((MessageListener<String, String>) record -> {
			try {
				var evento = objectMapper.readValue(record.value(), EventoMensaje.class);
				if (aceptaEvento(consumidor, evento)) {
					consumidor.consumir(evento);
				}
			} catch (Exception e) {
				throw new IllegalStateException("No fue posible procesar el mensaje", e);
			}
		});
		container.start();
	}

	private MessagingProperties.DestinoProperties destino(String nombre) {
		var destino = properties.getDestinos().get(nombre);
		if (destino == null || destino.getNombre() == null || destino.getNombre().isBlank()) {
			throw new IllegalStateException("Destino de mensajería no configurado: " + nombre);
		}
		if (!soporta(nombre)) {
			throw new IllegalStateException("El destino no pertenece a una conexión Kafka: " + nombre);
		}
		return destino;
	}

	private MessagingProperties.ConexionProperties conexion(String nombre) {
		var conexion = properties.getConexiones().get(nombre);
		if (conexion == null) {
			throw new IllegalStateException("Conexión de mensajería no configurada: " + nombre);
		}
		return conexion;
	}

	private KafkaTemplate<String, String> template(String conexion) {
		return templates.computeIfAbsent(conexion,
				nombre -> new KafkaTemplate<>(crearProducerFactory(conexion(nombre))));
	}

	private ConsumerFactory<String, String> consumerFactory(String conexion) {
		return consumerFactories.computeIfAbsent(conexion, nombre -> crearConsumerFactory(conexion(nombre)));
	}

	private ConsumerFactory<String, String> crearConsumerFactory(MessagingProperties.ConexionProperties conexion) {
		var p = conexion.getKafka();
		var c = new HashMap<String, Object>();
		c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, p.getBootstrapServers());
		c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, p.getConsumer().getAutoOffsetReset());
		c.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, p.getConsumer().isEnableAutoCommit());
		c.put(ConsumerConfig.CLIENT_ID_CONFIG, p.getClientId());
		c.put("security.protocol", p.getSecurityProtocol());
		return new DefaultKafkaConsumerFactory<>(c);
	}

	private ProducerFactory<String, String> crearProducerFactory(MessagingProperties.ConexionProperties conexion) {
		var p = conexion.getKafka();
		var c = new HashMap<String, Object>();
		c.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, p.getBootstrapServers());
		c.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		c.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		c.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, p.getProducer().getAcks());
		c.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, p.getClientId());
		c.put("security.protocol", p.getSecurityProtocol());
		return new DefaultKafkaProducerFactory<>(c);
	}

	private boolean aceptaEvento(ConsumidorMensajes consumidor, EventoMensaje evento) {
		var tipos = consumidor.tiposEvento();
		return tipos == null || tipos.isEmpty() || tipos.contains(evento.tipo());
	}
}
