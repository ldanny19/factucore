package ec.dalara.factucore.messaging.api;

import org.springframework.kafka.annotation.KafkaListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@KafkaListener(
        topics = "#{__listener.topico()}",
        groupId = "#{__listener.grupo()}",
        containerFactory = "kafkaListenerContainerFactory"
)
public @interface EscucharMensajes {
}
