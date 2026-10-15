package ec.dalara.factucore.notificacion.adapter.in.messaging;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.ConsumidorMensajes;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import ec.dalara.factucore.notificacion.application.service.NotificacionService;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteAutorizadoConsumidor implements ConsumidorMensajes {

	public static final String TIPO_EVENTO = "COMPROBANTE_AUTORIZADO";
	private static final String DESTINO = "notificacion";
	private static final String GRUPO = "factucore-notificacion";
	private static final Logger LOGGER = LoggerFactory.getLogger(ComprobanteAutorizadoConsumidor.class);

	private final ObjectMapper objectMapper;
	private final NotificacionService service;
	private final MessageResolver messageResolver;

	@Override
	public String destino() {
		return DESTINO;
	}

	@Override
	public String grupo() {
		return GRUPO;
	}

	@Override
	public Set<String> tiposEvento() {
		return Set.of(TIPO_EVENTO);
	}

	@Override
	public void consumir(EventoMensaje evento) {
		LOGGER.info("Inicia procesamiento de comprobante autorizado. ID_EVENTO={}, ID_TRANSACCION={}.", evento.id(),
				evento.correlationId());
		try {
			ComprobanteAutorizado comprobante = objectMapper.treeToValue(evento.payload(), ComprobanteAutorizado.class);
			service.notificar(evento, comprobante);
		} catch (Exception exception) {
			LOGGER.error("Error procesando comprobante autorizado. ID_EVENTO={}, ID_TRANSACCION={}.", evento.id(),
					evento.correlationId(), exception);
			throw new IllegalStateException(
					messageResolver.resolver(MessageCodes.EVENTO_PROCESAMIENTO_ERROR, evento.id()), exception);
		}
	}
}
