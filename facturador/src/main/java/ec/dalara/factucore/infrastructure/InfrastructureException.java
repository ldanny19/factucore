package ec.dalara.factucore.infrastructure;

public class InfrastructureException extends RuntimeException {

    private final String codigo;
    private final Object[] parametros;

    public InfrastructureException(String codigo, Object... parametros) {
        super(codigo);
        this.codigo = codigo;
        this.parametros = parametros;
    }

    public String getCodigo() {
        return codigo;
    }

    public Object[] getParametros() {
        return parametros;
    }
}