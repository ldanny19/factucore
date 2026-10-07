package ec.dalara.factucore.domain.comprobante;

import java.time.LocalDateTime;

public class ComprobanteAuditoriaModel {

	private String etapa;
	private String resultado;
	private String estadoAnterior;
	private String estadoNuevo;
	private String codigoError;
	private String mensajeError;
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;
	private Integer intento;
	private LocalDateTime fecha;

	public ComprobanteAuditoriaModel(String etapa, String resultado, String estadoAnterior, String estadoNuevo,
			String codigoError, String mensajeError, LocalDateTime fechaInicio, LocalDateTime fechaFin, Integer intento,
			LocalDateTime fecha) {
		this.etapa = etapa;
		this.resultado = resultado;
		this.estadoAnterior = estadoAnterior;
		this.estadoNuevo = estadoNuevo;
		this.codigoError = codigoError;
		this.mensajeError = mensajeError;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.intento = intento;
		this.fecha = fecha;
	}

	public ComprobanteAuditoriaModel(String estadoAnterior, String estadoNuevo, String codigoError, String mensajeError,
			LocalDateTime fecha) {
		this(null, null, estadoAnterior, estadoNuevo, codigoError, mensajeError, null, null, null, fecha);
	}

	public String getEtapa() {
		return etapa;
	}

	public String getResultado() {
		return resultado;
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

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public Integer getIntento() {
		return intento;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}
}
