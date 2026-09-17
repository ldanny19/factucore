package ec.dalara.factucore.domain.comprobante;

import java.math.BigDecimal;
import java.util.Objects;

public class ComprobanteDetalleImpuestoModel {

    private String codigoImpuesto;
    private String codigoPorcentaje;
    private BigDecimal tarifa;
    private BigDecimal baseImponible;
    private BigDecimal valor;
    private BigDecimal valorDevolucionIva;

    public ComprobanteDetalleImpuestoModel(
            String codigoImpuesto,
            String codigoPorcentaje,
            BigDecimal tarifa,
            BigDecimal baseImponible,
            BigDecimal valor,
            BigDecimal valorDevolucionIva
    ) {
        this.codigoImpuesto = Objects.requireNonNull(codigoImpuesto);
        this.codigoPorcentaje = Objects.requireNonNull(codigoPorcentaje);
        this.tarifa = tarifa;
        this.baseImponible = Objects.requireNonNull(baseImponible);
        this.valor = Objects.requireNonNull(valor);
        this.valorDevolucionIva = valorDevolucionIva;
    }

    public String getCodigoImpuesto() {
        return codigoImpuesto;
    }

    public String getCodigoPorcentaje() {
        return codigoPorcentaje;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getValorDevolucionIva() {
        return valorDevolucionIva;
    }
}