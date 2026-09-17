package ec.dalara.factucore.domain.documentoxsd;

public class AtributoXsdModel {

    private final Long elementoXsdId;
    private final String nombre;
    private final String tipoDato;
    private final Boolean obligatorio;
    private final String valorPredeterminado;
    private final String patron;

    public AtributoXsdModel(
            Long elementoXsdId,
            String nombre,
            String tipoDato,
            Boolean obligatorio,
            String valorPredeterminado,
            String patron
    ) {
        this.elementoXsdId = elementoXsdId;
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.obligatorio = obligatorio;
        this.valorPredeterminado = valorPredeterminado;
        this.patron = patron;
    }

    public Long getElementoXsdId() {
        return elementoXsdId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public String getValorPredeterminado() {
        return valorPredeterminado;
    }

    public String getPatron() {
        return patron;
    }
}