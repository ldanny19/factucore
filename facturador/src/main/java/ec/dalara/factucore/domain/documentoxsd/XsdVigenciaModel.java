package ec.dalara.factucore.domain.documentoxsd;

import java.time.LocalDateTime;
import java.util.Objects;

public class XsdVigenciaModel {

    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;

    public XsdVigenciaModel(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        this.fechaInicio = Objects.requireNonNull(fechaInicio);
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public boolean estaVigente(LocalDateTime fecha) {
        Objects.requireNonNull(fecha);

        boolean inicioValido = !fecha.isBefore(fechaInicio);
        boolean finValido = fechaFin == null || !fecha.isAfter(fechaFin);

        return inicioValido && finValido;
    }
}