package ec.dalara.factucore.messaging.infrastructure.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.*;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class RabbitBrokerMensajeria implements BrokerMensajeria {

    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, CachingConnectionFactory> connectionFactories = new ConcurrentHashMap<>();
    private final Map<String, RabbitTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, RabbitAdmin> admins = new ConcurrentHashMap<>();

    @Override
    public TipoBroker tipo() {
        return TipoBroker.RABBITMQ;
    }

    @Override
    public boolean soporta(String destino) {
        var config = properties.getDestinos().get(destino);
        if (config == null) return false;
        var conexion = properties.getConexiones().get(config.getConexion());
        return conexion != null && conexion.getTipo() == TipoBroker.RABBITMQ;
    }

    @Override
    public void publicar(String destino, EventoMensaje evento) {
        try {
            var configuracion = destino(destino);
            var admin = admin(configuracion.getConexion());
            var template = template(configuracion.getConexion());
            var exchange = exchange(configuracion.getConexion(), configuracion.getNombre());

            admin.declareExchange(exchange);
            template.convertAndSend(
                    exchange.getName(),
                    configuracion.getNombre(),
                    objectMapper.writeValueAsBytes(evento));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible publicar el mensaje", e);
        }
    }

    @Override
    public void registrar(ConsumidorMensajes consumidor) {
        var configuracion = destino(consumidor.destino());
        var conexion = configuracion.getConexion();
        var admin = admin(conexion);
        var exchange = exchange(conexion, configuracion.getNombre());

        var queue = QueueBuilder.durable(queueName(configuracion.getNombre(), consumidor.grupo())).build();
        var binding = BindingBuilder.bind(queue)
                .to(exchange)
                .with(configuracion.getNombre())
                .noargs();

        admin.declareExchange(exchange);
        admin.declareQueue(queue);
        admin.declareBinding(binding);

        var container = new SimpleMessageListenerContainer(connectionFactory(conexion));
        container.setQueueNames(queue.getName());
        container.setConcurrentConsumers(
                Math.toIntExact(conexion(conexion).getRabbitmq().getConcurrency()));
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        if (properties.getRetry().isDlqHabilitada()) {
            var dlqExchange = exchange(
                    conexion,
                    configuracion.getNombre() + properties.getRetry().getSufijoDlq());
            admin.declareExchange(dlqExchange);

            var recoverer = new RepublishMessageRecoverer(
                    template(conexion),
                    dlqExchange.getName(),
                    configuracion.getNombre());

            container.setAdviceChain(RetryInterceptorBuilder.stateless()
                    .maxAttempts(Math.toIntExact(properties.getRetry().getMaxIntentos()))
                    .backOffOptions(
                            properties.getRetry().getIntervaloMs(),
                            2.0,
                            properties.getRetry().getIntervaloMs() * 8)
                    .recoverer(recoverer)
                    .build());
        }

        container.setMessageListener(message -> {
            try {
                var evento = objectMapper.readValue(message.getBody(), EventoMensaje.class);
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
            throw new IllegalStateException("El destino no pertenece a una conexión RabbitMQ: " + nombre);
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

    private CachingConnectionFactory connectionFactory(String conexion) {
        return connectionFactories.computeIfAbsent(conexion, nombre -> {
            var p = conexion(nombre).getRabbitmq();
            var factory = new CachingConnectionFactory();
            factory.setAddresses(p.getAddresses());
            factory.setUsername(p.getUsername());
            factory.setPassword(p.getPassword());
            factory.setVirtualHost(p.getVirtualHost());
            return factory;
        });
    }

    private RabbitTemplate template(String conexion) {
        return templates.computeIfAbsent(conexion,
                nombre -> new RabbitTemplate(connectionFactory(nombre)));
    }

    private RabbitAdmin admin(String conexion) {
        return admins.computeIfAbsent(conexion,
                nombre -> new RabbitAdmin(connectionFactory(nombre)));
    }

    private Exchange exchange(String conexion, String nombre) {
        var p = conexion(conexion).getRabbitmq();
        return switch (p.getExchangeType().toLowerCase()) {
            case "direct" -> ExchangeBuilder.directExchange(
                    p.getExchangePrefix() + "." + nombre).durable(p.isDurable()).build();
            case "fanout" -> ExchangeBuilder.fanoutExchange(
                    p.getExchangePrefix() + "." + nombre).durable(p.isDurable()).build();
            default -> ExchangeBuilder.topicExchange(
                    p.getExchangePrefix() + "." + nombre).durable(p.isDurable()).build();
        };
    }

    private String queueName(String destino, String grupo) {
        return destino + "." + grupo;
    }

    private boolean aceptaEvento(ConsumidorMensajes consumidor, EventoMensaje evento) {
        var tipos = consumidor.tiposEvento();
        return tipos == null || tipos.isEmpty() || tipos.contains(evento.tipo());
    }
}
