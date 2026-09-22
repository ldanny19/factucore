package ec.dalara.factucore.application;

public class ApplicationException extends RuntimeException {

    private final String codigo;
    private final Object[] parametros;

    public ApplicationException(String codigo, Object... parametros) {
        this(codigo, null, parametros);
    }

    public ApplicationException(String codigo, Throwable causa, Object... parametros) {
        super(codigo, causa);
        this.codigo = codigo;
        this.parametros = parametros == null ? new Object[0] : parametros.clone();
    }

    public String getCodigo() {
        return codigo;
    }

    public Object[] getParametros() {
        return parametros.clone();
    }
}
