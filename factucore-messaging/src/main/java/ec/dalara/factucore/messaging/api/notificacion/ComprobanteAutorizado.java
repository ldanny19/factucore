package ec.dalara.factucore.messaging.api.notificacion;

import java.time.LocalDate;

public record ComprobanteAutorizado(String idTransaccion, String nombreCliente, String correo,
		LocalDate fechaComprobante, String nombreEmpresa, String rutaXmlAutorizado, String rutaRide) {
}
