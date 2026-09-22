package ec.dalara.factucore.application.workflow;

import lombok.Getter;

@Getter
public class WorkflowException extends RuntimeException {

	private final String codigo;
	private final Object[] parametros;

	public WorkflowException(String codigo, Object... parametros) {
		super(codigo);
		this.codigo = codigo;
		this.parametros = parametros == null ? new Object[0] : parametros.clone();
	}

	public WorkflowException(String codigo, Throwable causa, Object... parametros) {
		super(codigo, causa);
		this.codigo = codigo;
		this.parametros = parametros == null ? new Object[0] : parametros.clone();
	}
}