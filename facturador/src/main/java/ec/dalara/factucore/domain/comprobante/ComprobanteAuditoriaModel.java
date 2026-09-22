package ec.dalara.factucore.domain.comprobante;

import java.time.LocalDateTime;

public class ComprobanteAuditoriaModel {

	private String estadoAnterior;
	private String estadoNuevo;
	private String codigoError;
	private String mensajeError;
	private LocalDateTime fecha;

	public ComprobanteAuditoriaModel(String estadoAnterior, String estadoNuevo, String codigoError, String mensajeError,
			LocalDateTime fecha) {
		this.estadoAnterior = estadoAnterior;
		this.estadoNuevo = estadoNuevo;
		this.codigoError = codigoError;
		this.mensajeError = mensajeError;
		this.fecha = fecha;
	}

	public String getEstadoAnterior() {
		return estadoAnterior;
	}

	public String getEstadoNuevo() {
		return estadoNuevo;
	}

	public String getCodigoError() {
		return codigoError;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}
}