package ec.dalara.factucore.application.port.out.sri;

public record SriMensaje(
        String identificador,
        String mensaje,
        String informacionAdicional,
        String tipo
) {
}