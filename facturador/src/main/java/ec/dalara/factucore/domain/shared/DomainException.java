package ec.dalara.factucore.domain.shared;

public class DomainException extends RuntimeException {

    private final String codigo;
    private final Object[] parametros;

    public DomainException(String codigo, Object... parametros) {
        this(codigo, null, parametros);
    }

    public DomainException(String codigo, Throwable causa, Object... parametros) {
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
