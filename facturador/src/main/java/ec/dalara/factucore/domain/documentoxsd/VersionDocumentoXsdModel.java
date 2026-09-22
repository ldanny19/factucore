package ec.dalara.factucore.domain.documentoxsd;

import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.DomainException;

public final class VersionDocumentoXsdModel {

	private final Long id;
	private final Long documentoXsdId;
	private final String version;
	private final String namespaceXml;
	private final String elementoRaiz;
	private final LocalDateTime fechaInicio;
	private final LocalDateTime fechaFin;

	public VersionDocumentoXsdModel(Long id, Long documentoXsdId, String version, String namespaceXml,
			String elementoRaiz, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		if (documentoXsdId == null) {
			throw new DomainException("FACTUCORE.VERSION_DOCUMENTO_XSD.DOCUMENTO.REQUERIDO");
		}

		if (version == null || version.isBlank()) {
			throw new DomainException("FACTUCORE.VERSION_DOCUMENTO_XSD.VERSION.REQUERIDA");
		}

		if (fechaInicio == null) {
			throw new DomainException("FACTUCORE.VERSION_DOCUMENTO_XSD.FECHA_INICIO.REQUERIDA");
		}

		if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
			throw new DomainException("FACTUCORE.VERSION_DOCUMENTO_XSD.RANGO_FECHAS.INVALIDO");
		}

		this.id = id;
		this.documentoXsdId = documentoXsdId;
		this.version = version;
		this.namespaceXml = namespaceXml;
		this.elementoRaiz = elementoRaiz;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}

	public boolean estaVigente(LocalDateTime fecha) {
		if (fecha == null || fecha.isBefore(fechaInicio)) {
			return false;
		}

		return fechaFin == null || !fecha.isAfter(fechaFin);
	}

	public Long getId() {
		return id;
	}

	public Long getDocumentoXsdId() {
		return documentoXsdId;
	}

	public String getVersion() {
		return version;
	}

	public String getNamespaceXml() {
		return namespaceXml;
	}

	public String getElementoRaiz() {
		return elementoRaiz;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}
}