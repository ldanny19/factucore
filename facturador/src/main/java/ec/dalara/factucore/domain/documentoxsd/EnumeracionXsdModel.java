package ec.dalara.factucore.domain.documentoxsd;

import java.util.Objects;

public final class EnumeracionXsdModel {

    private final Long id;
    private final Long elementoXsdId;
    private final String valor;
    private final String descripcion;
    private final Integer orden;

    public EnumeracionXsdModel(Long id, Long elementoXsdId, String valor, String descripcion, Integer orden) {
        this.id = id;
        this.elementoXsdId = Objects.requireNonNull(elementoXsdId, "elementoXsdId");
        this.valor = Objects.requireNonNull(valor, "valor");
        this.descripcion = descripcion;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public Long getElementoXsdId() { return elementoXsdId; }
    public String getValor() { return valor; }
    public String getDescripcion() { return descripcion; }
    public Integer getOrden() { return orden; }
}
