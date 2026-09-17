package ec.dalara.factucore.domain.catalogo;

import java.util.Objects;

public class CatalogoModel {

    private final String codigo;
    private final String nombre;
    private final String descripcion;

    public CatalogoModel(
            String codigo,
            String nombre,
            String descripcion
    ) {
        this.codigo = Objects.requireNonNull(codigo);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}