package ec.dalara.factucore.domain.comprobante;

import ec.dalara.factucore.domain.shared.DomainException;

import java.time.LocalDate;
import java.util.Objects;

public final class ComprobanteModel {

    private final Long id;
    private final Long empresaId;
    private final Long establecimientoId;
    private final Long puntoEmisionId;
    private final String codigoDocumento;
    private final NumeroComprobante numeroComprobante;
    private final LocalDate fechaEmision;

    private String estadoProceso;
    private ClaveAcceso claveAcceso;

    public ComprobanteModel(
            Long id,
            Long empresaId,
            Long establecimientoId,
            Long puntoEmisionId,
            String codigoDocumento,
            NumeroComprobante numeroComprobante,
            LocalDate fechaEmision) {

        if (empresaId == null) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.EMPRESA.REQUERIDA"
            );
        }

        if (establecimientoId == null) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.ESTABLECIMIENTO.REQUERIDO"
            );
        }

        if (puntoEmisionId == null) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.PUNTO_EMISION.REQUERIDO"
            );
        }

        if (codigoDocumento == null || codigoDocumento.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.CODIGO_DOCUMENTO.REQUERIDO"
            );
        }

        if (numeroComprobante == null) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.NUMERO.REQUERIDO"
            );
        }

        if (fechaEmision == null) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.FECHA_EMISION.REQUERIDA"
            );
        }

        this.id = id;
        this.empresaId = empresaId;
        this.establecimientoId = establecimientoId;
        this.puntoEmisionId = puntoEmisionId;
        this.codigoDocumento = codigoDocumento;
        this.numeroComprobante = numeroComprobante;
        this.fechaEmision = fechaEmision;
    }

    public void asignarClaveAcceso(ClaveAcceso claveAcceso) {
        if (claveAcceso == null) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.REQUERIDA"
            );
        }

        this.claveAcceso = claveAcceso;
    }

    public void cambiarEstadoProceso(String estadoProceso) {
        if (estadoProceso == null || estadoProceso.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.COMPROBANTE.ESTADO_PROCESO.REQUERIDO"
            );
        }

        this.estadoProceso = estadoProceso;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public Long getEstablecimientoId() {
        return establecimientoId;
    }

    public Long getPuntoEmisionId() {
        return puntoEmisionId;
    }

    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public NumeroComprobante getNumeroComprobante() {
        return numeroComprobante;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public String getEstadoProceso() {
        return estadoProceso;
    }

    public ClaveAcceso getClaveAcceso() {
        return claveAcceso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ComprobanteModel that)) {
            return false;
        }

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}