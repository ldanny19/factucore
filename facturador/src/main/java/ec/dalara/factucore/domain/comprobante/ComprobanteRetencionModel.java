package ec.dalara.factucore.domain.comprobante;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ComprobanteRetencionModel {

    private String codigoImpuesto;
    private String codigoRetencion;
    private BigDecimal porcentajeRetener;
    private BigDecimal baseImponible;
    private BigDecimal valorRetenido;
    private String numeroDocumentoSustento;
    private LocalDate fechaEmisionDocumentoSustento;

    public ComprobanteRetencionModel(
            String codigoImpuesto,
            String codigoRetencion,
            BigDecimal porcentajeRetener,
            BigDecimal baseImponible,
            BigDecimal valorRetenido,
            String numeroDocumentoSustento,
            LocalDate fechaEmisionDocumentoSustento
    ) {
        this.codigoImpuesto = codigoImpuesto;
        this.codigoRetencion = codigoRetencion;
        this.porcentajeRetener = porcentajeRetener;
        this.baseImponible = baseImponible;
        this.valorRetenido = valorRetenido;
        this.numeroDocumentoSustento = numeroDocumentoSustento;
        this.fechaEmisionDocumentoSustento = fechaEmisionDocumentoSustento;
    }

    public String getCodigoImpuesto() {
        return codigoImpuesto;
    }

    public String getCodigoRetencion() {
        return codigoRetencion;
    }

    public BigDecimal getPorcentajeRetener() {
        return porcentajeRetener;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public BigDecimal getValorRetenido() {
        return valorRetenido;
    }

    public String getNumeroDocumentoSustento() {
        return numeroDocumentoSustento;
    }

    public LocalDate getFechaEmisionDocumentoSustento() {
        return fechaEmisionDocumentoSustento;
    }
}