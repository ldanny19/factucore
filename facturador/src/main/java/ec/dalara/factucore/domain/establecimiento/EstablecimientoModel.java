package ec.dalara.factucore.domain.establecimiento;

import ec.dalara.factucore.domain.shared.DomainException;

import java.util.Objects;

public final class EstablecimientoModel {

    private final Long id;
    private final Long empresaId;
    private final String codigo;
    private final String nombre;
    private final String direccion;

    public EstablecimientoModel(
            Long id,
            Long empresaId,
            String codigo,
            String nombre,
            String direccion) {

        if (empresaId == null) {
            throw new DomainException(
                    "FACTUCORE.ESTABLECIMIENTO.EMPRESA.REQUERIDA"
            );
        }

        if (codigo == null || codigo.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.ESTABLECIMIENTO.CODIGO.REQUERIDO"
            );
        }

        this.id = id;
        this.empresaId = empresaId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EstablecimientoModel that)) {
            return false;
        }

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}