package ec.dalara.factucore.messaging.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ec.dalara.factucore.messaging.api.TipoBroker;

@ConfigurationProperties(prefix = "factucore.messaging")
public class MessagingProperties {

	private boolean enabled;

	private Map<String, ConexionProperties> conexiones = new LinkedHashMap<>();

	private Map<String, DestinoProperties> destinos = new LinkedHashMap<>();

	private ResilienciaProperties resiliencia = new ResilienciaProperties();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, ConexionProperties> getConexiones() {
		return conexiones;
	}

	public void setConexiones(Map<String, ConexionProperties> conexiones) {
		this.conexiones = conexiones;
	}

	public Map<String, DestinoProperties> getDestinos() {
		return destinos;
	}

	public void setDestinos(Map<String, DestinoProperties> destinos) {
		this.destinos = destinos;
	}

	public ResilienciaProperties getResiliencia() {
		return resiliencia;
	}

	public void setResiliencia(ResilienciaProperties resiliencia) {
		this.resiliencia = resiliencia;
	}

	@Deprecated
	public RetryProperties getRetry() {
		return resiliencia.getReintentos();
	}

	public static class ConexionProperties {

		private TipoBroker tipo;

		private KafkaProperties kafka = new KafkaProperties();

		private RabbitmqProperties rabbitmq = new RabbitmqProperties();

		public TipoBroker getTipo() {
			return tipo;
		}

		public void setTipo(TipoBroker tipo) {
			this.tipo = tipo;
		}

		public KafkaProperties getKafka() {
			return kafka;
		}

		public void setKafka(KafkaProperties kafka) {
			this.kafka = kafka;
		}

		public RabbitmqProperties getRabbitmq() {
			return rabbitmq;
		}

		public void setRabbitmq(RabbitmqProperties rabbitmq) {
			this.rabbitmq = rabbitmq;
		}
	}

	public static class DestinoProperties {

		private String conexion;

		private String nombre;

		public String getConexion() {
			return conexion;
		}

		public void setConexion(String conexion) {
			this.conexion = conexion;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
	}

	public static class KafkaProperties {

		private String bootstrapServers;

		private String securityProtocol;

		private String clientId;

		private ProducerProperties producer = new ProducerProperties();

		private ConsumerProperties consumer = new ConsumerProperties();

		public String getBootstrapServers() {
			return bootstrapServers;
		}

		public void setBootstrapServers(String bootstrapServers) {
			this.bootstrapServers = bootstrapServers;
		}

		public String getSecurityProtocol() {
			return securityProtocol;
		}

		public void setSecurityProtocol(String securityProtocol) {
			this.securityProtocol = securityProtocol;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public ProducerProperties getProducer() {
			return producer;
		}

		public void setProducer(ProducerProperties producer) {
			this.producer = producer;
		}

		public ConsumerProperties getConsumer() {
			return consumer;
		}

		public void setConsumer(ConsumerProperties consumer) {
			this.consumer = consumer;
		}
	}

	public static class ProducerProperties {

		private Map<String, Object> propiedadesAdicionales = new LinkedHashMap<>();

		public Map<String, Object> getPropiedadesAdicionales() { return propiedadesAdicionales; }
		public void setPropiedadesAdicionales(Map<String, Object> value) { propiedadesAdicionales = value; }

		private String acks;

		public String getAcks() {
			return acks;
		}

		public void setAcks(String acks) {
			this.acks = acks;
		}
	}

	public static class ConsumerProperties {

		private String autoOffsetReset;

		private Boolean enableAutoCommit;

		private Integer concurrency;

		private Map<String, Object> propiedadesAdicionales = new LinkedHashMap<>();

		public Map<String, Object> getPropiedadesAdicionales() { return propiedadesAdicionales; }
		public void setPropiedadesAdicionales(Map<String, Object> value) { propiedadesAdicionales = value; }

		public String getAutoOffsetReset() {
			return autoOffsetReset;
		}

		public void setAutoOffsetReset(String autoOffsetReset) {
			this.autoOffsetReset = autoOffsetReset;
		}

		public Boolean isEnableAutoCommit() {
			return enableAutoCommit;
		}

		public void setEnableAutoCommit(Boolean enableAutoCommit) {
			this.enableAutoCommit = enableAutoCommit;
		}

		public Integer getConcurrency() {
			return concurrency;
		}

		public void setConcurrency(Integer concurrency) {
			this.concurrency = concurrency;
		}
	}

	public static class RabbitmqProperties {

		private String addresses;

		private String username;

		private String password;

		private String virtualHost;

		private String exchangeType;

		private String exchangePrefix;

		private Boolean durable;

		private Integer concurrency;

		private Integer connectionTimeoutMs = 5000;

		private Integer channelCacheSize = 25;

		private Integer connectionLimit = 10;

		private Map<String, Object> propiedadesConexion = new LinkedHashMap<>();
		private Map<String, Object> propiedadesCliente = new LinkedHashMap<>();
		private Map<String, Object> propiedadesPublicador = new LinkedHashMap<>();
		private Map<String, Object> propiedadesConsumidor = new LinkedHashMap<>();

		public Map<String, Object> getPropiedadesConexion() { return propiedadesConexion; }
		public void setPropiedadesConexion(Map<String, Object> value) { propiedadesConexion = value; }
		public Map<String, Object> getPropiedadesCliente() { return propiedadesCliente; }
		public void setPropiedadesCliente(Map<String, Object> value) { propiedadesCliente = value; }
		public Map<String, Object> getPropiedadesPublicador() { return propiedadesPublicador; }
		public void setPropiedadesPublicador(Map<String, Object> value) { propiedadesPublicador = value; }
		public Map<String, Object> getPropiedadesConsumidor() { return propiedadesConsumidor; }
		public void setPropiedadesConsumidor(Map<String, Object> value) { propiedadesConsumidor = value; }

		public String getAddresses() {
			return addresses;
		}

		public void setAddresses(String addresses) {
			this.addresses = addresses;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getVirtualHost() {
			return virtualHost;
		}

		public void setVirtualHost(String virtualHost) {
			this.virtualHost = virtualHost;
		}

		public String getExchangeType() {
			return exchangeType;
		}

		public void setExchangeType(String exchangeType) {
			this.exchangeType = exchangeType;
		}

		public String getExchangePrefix() {
			return exchangePrefix;
		}

		public void setExchangePrefix(String exchangePrefix) {
			this.exchangePrefix = exchangePrefix;
		}

		public Boolean isDurable() {
			return durable;
		}

		public void setDurable(Boolean durable) {
			this.durable = durable;
		}

		public Integer getConcurrency() {
			return concurrency;
		}

		public void setConcurrency(Integer concurrency) {
			this.concurrency = concurrency;
		}

		public Integer getConnectionTimeoutMs() {
			return connectionTimeoutMs;
		}

		public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
			this.connectionTimeoutMs = connectionTimeoutMs;
		}

		public Integer getChannelCacheSize() {
			return channelCacheSize;
		}

		public void setChannelCacheSize(Integer channelCacheSize) {
			this.channelCacheSize = channelCacheSize;
		}

		public Integer getConnectionLimit() {
			return connectionLimit;
		}

		public void setConnectionLimit(Integer connectionLimit) {
			this.connectionLimit = connectionLimit;
		}
	}

	public static class ResilienciaProperties {

		private RetryProperties reintentos = new RetryProperties();

		private TimeoutProperties timeout = new TimeoutProperties();

		public RetryProperties getReintentos() {
			return reintentos;
		}

		public void setReintentos(RetryProperties reintentos) {
			this.reintentos = reintentos;
		}

		public TimeoutProperties getTimeout() {
			return timeout;
		}

		public void setTimeout(TimeoutProperties timeout) {
			this.timeout = timeout;
		}
	}

	public static class TimeoutProperties {

		private Long conexionMs = 5000L;

		private Long envioMs = 10000L;

		public Long getConexionMs() {
			return conexionMs;
		}

		public void setConexionMs(Long conexionMs) {
			this.conexionMs = conexionMs;
		}

		public Long getEnvioMs() {
			return envioMs;
		}

		public void setEnvioMs(Long envioMs) {
			this.envioMs = envioMs;
		}
	}

	public static class RetryProperties {

		private Long intervaloMs = 5000L;

		private Long maxIntentos = 3L;

		private Boolean dlqHabilitada = true;

		private String sufijoDlq = ".DLQ";

		public Long getIntervaloMs() {
			return intervaloMs;
		}

		public void setIntervaloMs(Long intervaloMs) {
			this.intervaloMs = intervaloMs;
		}

		public Long getMaxIntentos() {
			return maxIntentos;
		}

		public void setMaxIntentos(Long maxIntentos) {
			this.maxIntentos = maxIntentos;
		}

		public Boolean isDlqHabilitada() {
			return dlqHabilitada;
		}

		public void setDlqHabilitada(Boolean dlqHabilitada) {
			this.dlqHabilitada = dlqHabilitada;
		}

		public String getSufijoDlq() {
			return sufijoDlq;
		}

		public void setSufijoDlq(String sufijoDlq) {
			this.sufijoDlq = sufijoDlq;
		}
	}
}
