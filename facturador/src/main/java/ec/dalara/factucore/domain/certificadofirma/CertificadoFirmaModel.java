package ec.dalara.factucore.domain.certificadofirma;

import java.time.LocalDateTime;
import java.util.Objects;

public class CertificadoFirmaModel {

	private final Long empresaId;
	private final String nombreArchivo;
	private final String rutaCertificado;
	private final LocalDateTime fechaInicio;
	private final LocalDateTime fechaFin;

	public CertificadoFirmaModel(Long empresaId, String nombreArchivo, String rutaCertificado,
			LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		this.empresaId = Objects.requireNonNull(empresaId);
		this.rutaCertificado = Objects.requireNonNull(rutaCertificado);
		this.nombreArchivo = nombreArchivo;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public Long getEmpresaId() {
		return empresaId;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public String getRutaCertificado() {
		return rutaCertificado;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}
}