package ec.dalara.factucore.domain.validacion;

public record ValidationViolation(String codigo, String ruta, Object... parametros) {
    public ValidationViolation {
        parametros = parametros == null ? new Object[0] : parametros.clone();
    }
    @Override public Object[] parametros() { return parametros.clone(); }
}
