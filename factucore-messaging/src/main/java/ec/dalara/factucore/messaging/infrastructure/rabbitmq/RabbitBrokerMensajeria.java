package ec.dalara.factucore.messaging.infrastructure.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.BrokerMensajeria;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.TipoBroker;
import ec.dalara.factucore.messaging.config.MessagingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;

@RequiredArgsConstructor
public class RabbitBrokerMensajeria implements BrokerMensajeria {

    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate template;
    private final ConnectionFactory connectionFactory;
    private final AmqpAdmin amqpAdmin;

    @Override
    public void publicar(String destino, EventoMensaje evento) {
        try {
            var exchange = exchange(destino);
            amqpAdmin.declareExchange(exchange);
            template.convertAndSend(
                    exchange.getName(),
                    destino,
                    objectMapper.writeValueAsBytes(evento));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible publicar el mensaje", e);
        }
    }

    @Override
    public void registrar(ConsumidorMensajes consumidor) {
        var exchange = exchange(consumidor.topico());
        var queue = QueueBuilder.durable(queueName(consumidor)).build();
        var binding = BindingBuilder.bind(queue)
                .to(exchange)
                .with(consumidor.topico())
                .noargs();

        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        var container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queue.getName());
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        if (properties.getRetry().isDlqHabilitada()) {
            var dlqExchange = exchange(consumidor.topico() + properties.getRetry().getSufijoDlq());
            amqpAdmin.declareExchange(dlqExchange);

            var recoverer = new RepublishMessageRecoverer(
                    template,
                    dlqExchange.getName(),
                    consumidor.topico());

            container.setAdviceChain(RetryInterceptorBuilder.stateless()
                    .maxAttempts((int) properties.getRetry().getMaxIntentos())
                    .backOffOptions(
                            properties.getRetry().getIntervaloMs(),
                            2.0,
                            properties.getRetry().getIntervaloMs() * 8)
                    .recoverer(recoverer)
                    .build());
        }

        container.setMessageListener(message -> {
            try {
                var evento = objectMapper.readValue(
                        message.getBody(), EventoMensaje.class);
                consumidor.consumir(evento);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "No fue posible procesar el mensaje", e);
            }
        });

        container.start();
    }

    private Exchange exchange(String destino) {
        return ExchangeBuilder.topicExchange(
                        properties.getRabbitmq().getExchangePrefix() + "." + destino)
                .durable(properties.getRabbitmq().isDurable())
                .build();
    }

    private String queueName(ConsumidorMensajes consumidor) {
        return properties.getRabbitmq().getExchangePrefix()
                + "." + consumidor.topico()
                + "." + consumidor.grupo();
    }

    @Override
    public TipoBroker tipo() {
        return TipoBroker.RABBITMQ;
    }
}
