package ec.dalara.factucore.messaging.infrastructure.kafka;

import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPublicadorMensajes implements PublicadorMensajes {

    private final KafkaTemplate<String, EventoMensaje> kafkaTemplate;

    @Override
    public void publicar(String topico, EventoMensaje evento) {
        kafkaTemplate.send(topico, evento.id(), evento);
    }
}
