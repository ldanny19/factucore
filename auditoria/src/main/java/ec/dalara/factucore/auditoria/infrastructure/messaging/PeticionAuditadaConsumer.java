package ec.dalara.factucore.auditoria.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.auditoria.PeticionAuditada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeticionAuditadaConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${factucore.auditoria.peticion.topico}",
            groupId = "${factucore.auditoria.peticion.grupo-consumidor}"
    )
    public void consumir(EventoMensaje evento) {
        PeticionAuditada peticion = objectMapper.convertValue(evento.payload(), PeticionAuditada.class);

        log.info(
                "Petición de auditoría recibida. eventoId={}, idTransaccion={}, endpoint={}, estadoHttp={}",
                evento.id(),
                peticion.idTransaccion(),
                peticion.endpoint(),
                peticion.estadoHttp()
        );

        // V1: únicamente recibe y valida el contrato.
        // La persistencia y consultas serán implementadas posteriormente.
    }
}
