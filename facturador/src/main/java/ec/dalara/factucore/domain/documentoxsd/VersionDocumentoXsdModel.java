package ec.dalara.factucore.domain.documentoxsd;

import ec.dalara.factucore.domain.shared.DomainException;

import java.time.LocalDateTime;

public final class VersionDocumentoXsdModel {

    private final Long id;
    private final Long documentoXsdId;
    private final String version;
    private final String versionXsd;
    private final String contenidoXsd;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;

    public VersionDocumentoXsdModel(
            Long id,
            Long documentoXsdId,
            String version,
            String versionXsd,
            String contenidoXsd,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        if (documentoXsdId == null) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.DOCUMENTO.REQUERIDO"
            );
        }

        if (version == null || version.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.VERSION.REQUERIDA"
            );
        }

        if (versionXsd == null || versionXsd.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.VERSION_XSD.REQUERIDA"
            );
        }

        if (contenidoXsd == null || contenidoXsd.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.CONTENIDO_XSD.REQUERIDO"
            );
        }

        if (fechaInicio == null) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.FECHA_INICIO.REQUERIDA"
            );
        }

        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new DomainException(
                    "FACTUCORE.VERSION_DOCUMENTO_XSD.RANGO_FECHAS.INVALIDO"
            );
        }

        this.id = id;
        this.documentoXsdId = documentoXsdId;
        this.version = version;
        this.versionXsd = versionXsd;
        this.contenidoXsd = contenidoXsd;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public boolean estaVigente(LocalDateTime fecha) {
        if (fecha == null || fecha.isBefore(fechaInicio)) {
            return false;
        }

        return fechaFin == null || !fecha.isAfter(fechaFin);
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentoXsdId() {
        return documentoXsdId;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionXsd() {
        return versionXsd;
    }

    public String getContenidoXsd() {
        return contenidoXsd;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
}