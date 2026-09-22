package ec.dalara.factucore.domain.secuencial;

import ec.dalara.factucore.domain.shared.DomainException;

public final class SecuencialModel {

	private static final long MAXIMO_SECUENCIAL = 999_999_999L;

	private final Long id;
	private final Long puntoEmisionId;
	private final String codigoDocumento;
	private long ultimoSecuencial;

	public SecuencialModel(Long id, Long puntoEmisionId, String codigoDocumento, long ultimoSecuencial) {

		if (puntoEmisionId == null) {
			throw new DomainException("FACTUCORE.SECUENCIAL.PUNTO_EMISION.REQUERIDO");
		}

		if (codigoDocumento == null || codigoDocumento.isBlank()) {
			throw new DomainException("FACTUCORE.SECUENCIAL.CODIGO_DOCUMENTO.REQUERIDO");
		}

		if (ultimoSecuencial < 0 || ultimoSecuencial > MAXIMO_SECUENCIAL) {
			throw new DomainException("FACTUCORE.SECUENCIAL.VALOR.INVALIDO");
		}

		this.id = id;
		this.puntoEmisionId = puntoEmisionId;
		this.codigoDocumento = codigoDocumento;
		this.ultimoSecuencial = ultimoSecuencial;
	}

	public long siguiente() {

		if (ultimoSecuencial >= MAXIMO_SECUENCIAL) {
			throw new DomainException("FACTUCORE.SECUENCIAL.LIMITE_ALCANZADO");
		}

		return ++ultimoSecuencial;
	}

	public Long getId() {
		return id;
	}

	public Long getPuntoEmisionId() {
		return puntoEmisionId;
	}

	public String getCodigoDocumento() {
		return codigoDocumento;
	}

	public long getUltimoSecuencial() {
		return ultimoSecuencial;
	}
}