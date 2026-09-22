package ec.dalara.factucore.domain.firmaelectronica;

import ec.dalara.factucore.domain.shared.DomainException;

import java.time.LocalDateTime;

public final class CertificadoFirmaModel {

    private final Long id;
    private final Long empresaId;
    private final String nombreArchivo;
    private final String rutaCertificado;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;

    public CertificadoFirmaModel(
            Long id,
            Long empresaId,
            String nombreArchivo,
            String rutaCertificado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        if (empresaId == null) {
            throw new DomainException(
                    "FACTUCORE.CERTIFICADO_FIRMA.EMPRESA.REQUERIDA"
            );
        }

        if (rutaCertificado == null || rutaCertificado.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.CERTIFICADO_FIRMA.RUTA.REQUERIDA"
            );
        }

        if (fechaInicio != null
                && fechaFin != null
                && fechaFin.isBefore(fechaInicio)) {
            throw new DomainException(
                    "FACTUCORE.CERTIFICADO_FIRMA.VIGENCIA.INVALIDA"
            );
        }

        this.id = id;
        this.empresaId = empresaId;
        this.nombreArchivo = nombreArchivo;
        this.rutaCertificado = rutaCertificado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaCertificado() {
        return rutaCertificado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public boolean estaVigente(LocalDateTime fecha) {
        if (fecha == null) {
            return false;
        }

        boolean despuesInicio =
                fechaInicio == null || !fecha.isBefore(fechaInicio);

        boolean antesFin =
                fechaFin == null || !fecha.isAfter(fechaFin);

        return despuesInicio && antesFin;
    }
}