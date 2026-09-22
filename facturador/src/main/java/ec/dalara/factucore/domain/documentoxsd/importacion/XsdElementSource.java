package ec.dalara.factucore.domain.documentoxsd.importacion;

import java.math.BigDecimal;

public record XsdElementSource(
        String ruta, String nombre, String tipoDato, Integer orden,
        Integer minOcurrencias, Integer maxOcurrencias,
        Integer longitudMinima, Integer longitudMaxima,
        Integer digitosTotales, Integer decimales,
        BigDecimal valorMinimo, BigDecimal valorMaximo, String patron) {

    public boolean esRepetible() { return maxOcurrencias == null || maxOcurrencias > 1; }
    public boolean esObligatorio() { return minOcurrencias != null && minOcurrencias > 0; }
}
