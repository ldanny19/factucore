package ec.dalara.factucore.domain.configuracionempresa;

import java.time.LocalDateTime;
import java.util.Objects;

public class ConfiguracionEmpresaModel {

    private final Long empresaId;
    private final String clave;
    private final String valor;
    private final String tipoDato;
    private final LocalDateTime fechaVigenciaDesde;
    private final LocalDateTime fechaVigenciaHasta;

    public ConfiguracionEmpresaModel(
            Long empresaId,
            String clave,
            String valor,
            String tipoDato,
            LocalDateTime fechaVigenciaDesde,
            LocalDateTime fechaVigenciaHasta
    ) {
        this.empresaId = Objects.requireNonNull(empresaId);
        this.clave = Objects.requireNonNull(clave);
        this.tipoDato = Objects.requireNonNull(tipoDato);
        this.fechaVigenciaDesde = Objects.requireNonNull(fechaVigenciaDesde);
        this.valor = valor;
        this.fechaVigenciaHasta = fechaVigenciaHasta;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getClave() {
        return clave;
    }

    public String getValor() {
        return valor;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public LocalDateTime getFechaVigenciaDesde() {
        return fechaVigenciaDesde;
    }

    public LocalDateTime getFechaVigenciaHasta() {
        return fechaVigenciaHasta;
    }
}