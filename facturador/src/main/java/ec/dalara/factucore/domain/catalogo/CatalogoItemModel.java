package ec.dalara.factucore.domain.catalogo;

import java.util.Objects;

public class CatalogoItemModel {

    private final Long catalogoId;
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final Integer orden;

    public CatalogoItemModel(
            Long catalogoId,
            String codigo,
            String nombre,
            String descripcion,
            Integer orden
    ) {
        this.catalogoId = Objects.requireNonNull(catalogoId);
        this.codigo = Objects.requireNonNull(codigo);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
        this.orden = orden;
    }

    public Long getCatalogoId() {
        return catalogoId;
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

    public Integer getOrden() {
        return orden;
    }
}