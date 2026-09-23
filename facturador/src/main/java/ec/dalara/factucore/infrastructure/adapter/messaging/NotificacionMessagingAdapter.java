package ec.dalara.factucore.infrastructure.adapter.messaging;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionMessagingAdapter implements NotificacionPort {

    private final PublicadorMensajes publicadorMensajes;
    private final ObjectMapper objectMapper;
    private final NotificacionMessagingProperties properties;

    @Override
    public void publicar(ContextoWorkflow contexto) {
        Comprobante comprobante = contexto.getComprobante();

        var payload = new LinkedHashMap<String, Object>();
        payload.put("comprobanteId", comprobante.getId());
        payload.put("claveAcceso", comprobante.getClaveAcceso());
        payload.put("numeroAutorizacion", comprobante.getNumeroAutorizacion());
        payload.put("rutaXmlFirmado", comprobante.getRutaXmlFirmado());
        payload.put("rutaRide", comprobante.getRutaRide());

        var evento = EventoMensaje.crear(
                properties.getTipoEvento(),
                properties.getVersionEvento(),
                comprobante.getClaveAcceso(),
                objectMapper.valueToTree(payload));

        publicadorMensajes.publicar(properties.getTopico(), evento);
    }
}
