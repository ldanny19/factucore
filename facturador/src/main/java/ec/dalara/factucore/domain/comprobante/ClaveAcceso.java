package ec.dalara.factucore.domain.comprobante;

import ec.dalara.factucore.domain.shared.DomainException;

public record ClaveAcceso(String valor) {

    public ClaveAcceso {
        if (valor == null || valor.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.REQUERIDA"
            );
        }

        if (!valor.matches("\\d{49}")) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.FORMATO_INVALIDO"
            );
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}