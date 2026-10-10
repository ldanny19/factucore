package ec.dalara.factucore.infrastructure.adapter.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteEvidencia;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteEvidenciaRepository;
import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.PublicadorMensajes;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionMessagingAdapter implements NotificacionPort {

	private static final String DESTINO_NOTIFICACION = "notificacion";

	private static final String XML_AUTORIZADO = "XML_AUTORIZADO";
	private static final String RIDE = "RIDE";
	private static final String FACTURA = "factura";
	private static final String INFORMACION_ADICIONAL = "infoAdicional";
	private static final String CAMPO_ADICIONAL = "campoAdicional";
	private static final String NOMBRE = "nombre";
	private static final String VALOR = "valor";
	private static final String CORREO = "Correo";

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

		String correo = obtenerCorreo(contexto);

		String nombreCliente = comprobante.getRazonSocialReceptor();
		String nombreEmpresa = obtenerNombreEmpresa(comprobante);

		List<String> datosFaltantes = new ArrayList<>();
		if (nombreCliente == null || nombreCliente.isBlank()) {
			datosFaltantes.add("razonSocialReceptor");
		}
		if (correo == null || correo.isBlank()) {
			datosFaltantes.add("correo destinatario (infoAdicional.campoAdicional[nombre=Correo].valor)");
		}
		if (nombreEmpresa == null || nombreEmpresa.isBlank()) {
			datosFaltantes.add("razonSocialEmisor/nombreComercialEmisor");
		}
		if (!datosFaltantes.isEmpty()) {
			throw new IllegalStateException(MessageCodes.NOTIFICACION_DATOS_CLIENTE_REQUERIDOS
					+ ": " + String.join(", ", datosFaltantes));
		}

		var payload = new ComprobanteAutorizado(comprobante.getIdTransaccion(), nombreCliente, correo,
				comprobante.getFechaEmision(), nombreEmpresa, xmlAutorizado.getRutaArchivo(), ride.getRutaArchivo());

		var evento = EventoMensaje.crear(properties.getTipoEvento(), properties.getVersionEvento(),
				comprobante.getIdTransaccion(), objectMapper.valueToTree(payload));

		publicadorMensajes.publicar(DESTINO_NOTIFICACION, evento);
	}

	private String obtenerNombreEmpresa(ec.dalara.factucore.infrastructure.persistence.entity.Comprobante comprobante) {

		if (comprobante.getNombreComercialEmisor() != null && !comprobante.getNombreComercialEmisor().isBlank()) {
			return comprobante.getNombreComercialEmisor();
		}

		return comprobante.getRazonSocialEmisor();
	}

	private String obtenerCorreo(ContextoWorkflow contexto) {
		if (contexto.getSolicitud() == null || contexto.getSolicitud().getDatos() == null) {
			return null;
		}

		JsonNode datos = contexto.getSolicitud().getDatos();
		JsonNode camposAdicionales = datos.path(FACTURA).path(INFORMACION_ADICIONAL).path(CAMPO_ADICIONAL);

		if (camposAdicionales.isArray()) {
			for (JsonNode campo : camposAdicionales) {
				String nombre = campo.path(NOMBRE).asText();
				if (CORREO.equalsIgnoreCase(nombre)) {
					JsonNode valor = campo.path(VALOR);
					if (!valor.isMissingNode() && !valor.isNull() && !valor.isContainerNode()) {
						return valor.asText();
					}
				}
			}
		}

		// Compatibilidad con solicitudes que envían el correo como una propiedad directa.
		return buscarDato(datos, "correo", "email", "correoCliente", "correo_cliente");
	}

	private String buscarDato(JsonNode nodo, String... claves) {
		if (nodo == null || nodo.isNull()) {
			return null;
		}
		if (nodo.isObject()) {
			var campos = nodo.fields();
			while (campos.hasNext()) {
				var campo = campos.next();
				for (String clave : claves) {
					if (clave.equalsIgnoreCase(campo.getKey()) && !campo.getValue().isContainerNode()) {
						return campo.getValue().asText();
					}
				}
				String encontrado = buscarDato(campo.getValue(), claves);
				if (encontrado != null) {
					return encontrado;
				}
			}
		}
		if (nodo.isArray()) {
			for (JsonNode item : nodo) {
				String encontrado = buscarDato(item, claves);
				if (encontrado != null) {
					return encontrado;
				}
			}
		}
		return null;
	}
}
