package ec.dalara.factucore.domain.comprobante;

import ec.dalara.factucore.domain.shared.DomainException;

public record NumeroComprobante(String valor) {

	public NumeroComprobante {
		if (valor == null || valor.isBlank()) {
			throw new DomainException("FACTUCORE.NUMERO_COMPROBANTE.REQUERIDO");
		}

		if (!valor.matches("\\d{3}-\\d{3}-\\d{9}")) {
			throw new DomainException("FACTUCORE.NUMERO_COMPROBANTE.FORMATO_INVALIDO");
		}
	}

	@Override
	public String toString() {
		return valor;
	}
}