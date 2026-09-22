package ec.dalara.factucore.application;

public class ApplicationException extends RuntimeException {

	private final String codigo;
	private final Object[] parametros;

	public ApplicationException(String codigo, Object... parametros) {
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