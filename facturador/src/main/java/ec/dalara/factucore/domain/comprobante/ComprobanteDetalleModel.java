package ec.dalara.factucore.domain.comprobante;

import java.math.BigDecimal;
import java.util.Objects;

public class ComprobanteDetalleModel {

    private Integer numeroLinea;
    private String codigoPrincipal;
    private String codigoAuxiliar;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal precioTotalSinImpuesto;

    public ComprobanteDetalleModel(
            Integer numeroLinea,
            String codigoPrincipal,
            String codigoAuxiliar,
            String descripcion,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal descuento,
            BigDecimal precioTotalSinImpuesto
    ) {
        this.numeroLinea = Objects.requireNonNull(numeroLinea);
        this.codigoPrincipal = codigoPrincipal;
        this.codigoAuxiliar = codigoAuxiliar;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
        this.precioTotalSinImpuesto = precioTotalSinImpuesto;
    }

    public Integer getNumeroLinea() {
        return numeroLinea;
    }

    public String getCodigoPrincipal() {
        return codigoPrincipal;
    }

    public String getCodigoAuxiliar() {
        return codigoAuxiliar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getPrecioTotalSinImpuesto() {
        return precioTotalSinImpuesto;
    }
}