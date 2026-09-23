package ec.dalara.factucore.application.port.out.sri;

import java.util.List;

public record SriResponse(
        boolean exitoso,
        String estado,
        String identificador,
        List<SriMensaje> mensajes,
        String respuestaXml) {

    public SriResponse {
        mensajes = mensajes == null ? List.of() : List.copyOf(mensajes);
    }

    public SriResponse(boolean exitoso, String estado, String identificador, List<SriMensaje> mensajes) {
        this(exitoso, estado, identificador, mensajes, null);
    }

    public boolean tieneMensajes() {
        return !mensajes.isEmpty();
    }
}