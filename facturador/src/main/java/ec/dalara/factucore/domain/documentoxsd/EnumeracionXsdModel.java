package ec.dalara.factucore.domain.documentoxsd;

import java.util.Objects;

public class EnumeracionXsdModel {

	private final Long elementoXsdId;
	private final String valor;
	private final String descripcion;
	private final Integer orden;

	public EnumeracionXsdModel(Long elementoXsdId, String valor, String descripcion, Integer orden) {
		this.elementoXsdId = elementoXsdId;
		this.valor = Objects.requireNonNull(valor);
		this.descripcion = descripcion;
		this.orden = orden;
	}

	public Long getElementoXsdId() {
		return elementoXsdId;
	}

	public String getValor() {
		return valor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public Integer getOrden() {
		return orden;
	}
}