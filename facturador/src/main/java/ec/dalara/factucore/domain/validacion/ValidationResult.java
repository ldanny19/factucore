package ec.dalara.factucore.domain.validacion;

import java.util.List;

public record ValidationResult(List<ValidationViolation> violaciones) {
    public ValidationResult {
        violaciones = List.copyOf(violaciones);
    }
    public boolean esValido() { return violaciones.isEmpty(); }
}
