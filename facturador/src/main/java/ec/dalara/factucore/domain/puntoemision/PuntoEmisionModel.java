package ec.dalara.factucore.domain.puntoemision;

import java.util.Objects;

import ec.dalara.factucore.domain.shared.DomainException;

public final class PuntoEmisionModel {

	private final Long id;
	private final Long establecimientoId;
	private final String codigo;
	private final String nombre;

	public PuntoEmisionModel(Long id, Long establecimientoId, String codigo, String nombre) {

		if (establecimientoId == null) {
			throw new DomainException("FACTUCORE.PUNTO_EMISION.ESTABLECIMIENTO.REQUERIDO");
		}

		if (codigo == null || codigo.isBlank()) {
			throw new DomainException("FACTUCORE.PUNTO_EMISION.CODIGO.REQUERIDO");
		}

		this.id = id;
		this.establecimientoId = establecimientoId;
		this.codigo = codigo;
		this.nombre = nombre;
	}

	public Long getId() {
		return id;
	}

	public Long getEstablecimientoId() {
		return establecimientoId;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getNombre() {
		return nombre;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof PuntoEmisionModel that)) {
			return false;
		}

		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}