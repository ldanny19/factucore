package ec.dalara.factucore.domain.empresa;

import ec.dalara.factucore.domain.shared.DomainException;

public record Ruc(String valor) {

    public Ruc {
        if (valor == null || valor.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.RUC.REQUERIDO"
            );
        }

        if (!valor.matches("\\d{13}")) {
            throw new DomainException(
                    "FACTUCORE.RUC.FORMATO_INVALIDO"
            );
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}