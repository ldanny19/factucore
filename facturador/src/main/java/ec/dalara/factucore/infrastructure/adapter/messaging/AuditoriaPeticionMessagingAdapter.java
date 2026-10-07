package ec.dalara.factucore.infrastructure.adapter.messaging;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.AuditoriaPeticionPort;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.api.auditoria.RegistroAuditoriaHttp;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditoriaPeticionMessagingAdapter implements AuditoriaPeticionPort {

    private final PublicadorMensajes publicadorMensajes;
    private final ObjectMapper objectMapper;
    private final AuditoriaPeticionMessagingProperties properties;

    @Override
    public void publicar(RegistroAuditoriaHttp peticion) {
        if (!properties.isHabilitada() || peticion == null) {
            return;
        }

        var evento = EventoMensaje.crear(
                properties.getTipoEvento(),
                properties.getVersionEvento(),
                peticion.idTransaccion(),
                objectMapper.valueToTree(peticion)
        );

        publicadorMensajes.publicar(evento);
    }
}
