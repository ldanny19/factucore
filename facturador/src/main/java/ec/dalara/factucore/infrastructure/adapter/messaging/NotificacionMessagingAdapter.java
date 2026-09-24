package ec.dalara.factucore.infrastructure.adapter.messaging;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteEvidencia;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteEvidenciaRepository;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionMessagingAdapter implements NotificacionPort {

    private static final String XML_AUTORIZADO = "XML_AUTORIZADO";
    private static final String RIDE = "RIDE";

    private final PublicadorMensajes publicadorMensajes;
    private final ObjectMapper objectMapper;
    private final NotificacionMessagingProperties properties;
    private final ComprobanteEvidenciaRepository evidenciaRepository;

    @Override
    public void publicar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getComprobante() == null) {
            throw new IllegalArgumentException(MessageCodes.NOTIFICACION_COMPROBANTE_REQUERIDO);
        }

        var comprobante = contexto.getComprobante();

        ComprobanteEvidencia xmlAutorizado = evidenciaRepository
                .findByComprobanteIdAndTipoEvidenciaAndActualTrue(comprobante.getId(), XML_AUTORIZADO)
                .orElseThrow(() -> new IllegalStateException(MessageCodes.NOTIFICACION_XML_AUTORIZADO_REQUERIDO));

        ComprobanteEvidencia ride = evidenciaRepository
                .findByComprobanteIdAndTipoEvidenciaAndActualTrue(comprobante.getId(), RIDE)
                .orElseThrow(() -> new IllegalStateException(MessageCodes.NOTIFICACION_RIDE_REQUERIDO));

        String nombreCliente = obtenerDato(contexto, "nombreCliente", "nombre_cliente",
                "razonSocialReceptor", "razon_social_receptor");
        String correo = obtenerDato(contexto, "correo", "email", "correoCliente", "correo_cliente");

        if (nombreCliente == null || nombreCliente.isBlank() || correo == null || correo.isBlank()) {
            throw new IllegalStateException(MessageCodes.NOTIFICACION_DATOS_CLIENTE_REQUERIDOS);
        }

        var payload = new LinkedHashMap<String, Object>();
        payload.put("idTransaccion", comprobante.getIdTransaccion());
        payload.put("nombreCliente", nombreCliente);
        payload.put("correo", correo);
        payload.put("rutaXmlAutorizado", xmlAutorizado.getRutaArchivo());
        payload.put("rutaRide", ride.getRutaArchivo());

        var evento = EventoMensaje.crear(
                properties.getTipoEvento(),
                properties.getVersionEvento(),
                comprobante.getIdTransaccion(),
                objectMapper.valueToTree(payload));

        publicadorMensajes.publicar(properties.getTopico(), evento);
    }

    private String obtenerDato(ContextoWorkflow contexto, String... claves) {
        if (contexto.getSolicitud() == null || contexto.getSolicitud().getDatos() == null) {
            return null;
        }

        for (var dato : contexto.getSolicitud().getDatos()) {
            if (dato == null || dato.getKey() == null || dato.getValue() == null) {
                continue;
            }

            for (String clave : claves) {
                if (clave.equalsIgnoreCase(dato.getKey())) {
                    return dato.getValue();
                }
            }
        }

        return null;
    }
}
