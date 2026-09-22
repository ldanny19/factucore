package ec.dalara.factucore.domain.claveacceso;

import ec.dalara.factucore.domain.shared.DomainException;

public final class ClaveAccesoModel {

	private final String clave;
	private final String fechaEmision;
	private final String codigoDocumento;
	private final String ruc;
	private final String ambiente;
	private final String serie;
	private final String secuencial;
	private final String codigoNumerico;
	private final String tipoEmision;
	private final String digitoVerificador;

	public ClaveAccesoModel(String clave, String fechaEmision, String codigoDocumento, String ruc, String ambiente,
			String serie, String secuencial, String codigoNumerico, String tipoEmision, String digitoVerificador) {
		if (clave == null || clave.isBlank()) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.CLAVE.REQUERIDA");
		}

		if (clave.length() != 49) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.CLAVE.LONGITUD_INVALIDA");
		}

		this.clave = clave;
		this.fechaEmision = fechaEmision;
		this.codigoDocumento = codigoDocumento;
		this.ruc = ruc;
		this.ambiente = ambiente;
		this.serie = serie;
		this.secuencial = secuencial;
		this.codigoNumerico = codigoNumerico;
		this.tipoEmision = tipoEmision;
		this.digitoVerificador = digitoVerificador;
	}

	public String getClave() {
		return clave;
	}

	public String getFechaEmision() {
		return fechaEmision;
	}

	public String getCodigoDocumento() {
		return codigoDocumento;
	}

	public String getRuc() {
		return ruc;
	}

	public String getAmbiente() {
		return ambiente;
	}

	public String getSerie() {
		return serie;
	}

	public String getSecuencial() {
		return secuencial;
	}

	public String getCodigoNumerico() {
		return codigoNumerico;
	}

	public String getTipoEmision() {
		return tipoEmision;
	}

	public String getDigitoVerificador() {
		return digitoVerificador;
	}
}