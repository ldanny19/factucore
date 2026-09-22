package ec.dalara.factucore.domain.shared;

public class DomainException extends RuntimeException {

	private final String codigo;
	private final Object[] parametros;

	public DomainException(String codigo, Object... parametros) {
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