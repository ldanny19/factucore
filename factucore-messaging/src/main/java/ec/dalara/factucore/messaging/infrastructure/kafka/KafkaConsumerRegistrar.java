package ec.dalara.factucore.messaging.infrastructure.kafka;

import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaConsumerRegistrar {

    private final ApplicationContext applicationContext;
    private final ConcurrentKafkaListenerContainerFactory<String, EventoMensaje> factory;

    public KafkaConsumerRegistrar(
            ApplicationContext applicationContext,
            ConcurrentKafkaListenerContainerFactory<String, EventoMensaje> factory
    ) {
        this.applicationContext = applicationContext;
        this.factory = factory;
    }
}
