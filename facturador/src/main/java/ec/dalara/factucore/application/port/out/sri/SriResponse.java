package ec.dalara.factucore.application.port.out.sri;

import java.time.LocalDateTime;
import java.util.List;

public record SriResponse(boolean exitoso, String estado, String identificador, List<SriMensaje> mensajes,
		String respuestaXml, LocalDateTime fechaAutorizacion) {

	public SriResponse {
		mensajes = mensajes == null ? List.of() : List.copyOf(mensajes);
	}

	public SriResponse(boolean exitoso, String estado, String identificador, List<SriMensaje> mensajes) {
		this(exitoso, estado, identificador, mensajes, null, null);
	}

	public SriResponse(boolean exitoso, String estado, String identificador, List<SriMensaje> mensajes,
			String respuestaXml) {
		this(exitoso, estado, identificador, mensajes, respuestaXml, null);
	}

	public boolean tieneMensajes() {
		return !mensajes.isEmpty();
	}
}