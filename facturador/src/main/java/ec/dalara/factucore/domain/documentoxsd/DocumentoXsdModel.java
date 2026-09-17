package ec.dalara.factucore.domain.documentoxsd;

import java.util.Objects;

public class DocumentoXsdModel {

    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final String tipoDocumento;

    public DocumentoXsdModel(
            String codigo,
            String nombre,
            String descripcion,
            String tipoDocumento
    ) {
        this.codigo = Objects.requireNonNull(codigo);
        this.nombre = Objects.requireNonNull(nombre);
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento);
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }
}