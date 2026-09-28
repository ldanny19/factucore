package ec.dalara.factucore.notificacion.adapter.in.messaging;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import ec.dalara.factucore.notificacion.application.service.NotificacionService;
import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteAutorizadoConsumidor implements ConsumidorMensajes {
    public static final String TIPO_EVENTO = "COMPROBANTE_AUTORIZADO";
    private final ObjectMapper objectMapper;
    private final NotificacionService service;
    private final NotificacionProperties properties;
    private final MessageResolver messageResolver;

    @Override
    public String topico() { return properties.getTopico(); }

    @Override
    public String grupo() { return "factucore-notificacion"; }

    @Override
    public void consumir(EventoMensaje evento) {
        if (!TIPO_EVENTO.equals(evento.tipo())) return;
        try {
            service.notificar(objectMapper.treeToValue(evento.payload(), ComprobanteAutorizado.class));
        } catch (Exception e) {
            throw new IllegalStateException(
                    messageResolver.resolver(MessageCodes.EVENTO_PROCESAMIENTO_ERROR, evento.id()), e);
        }
    }
}
