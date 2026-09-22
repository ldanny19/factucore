package ec.dalara.factucore.domain.empresa;

import java.util.Objects;

import ec.dalara.factucore.domain.shared.DomainException;

public final class EmpresaModel {

	private final Long id;
	private final String ruc;
	private final String razonSocial;
	private final String nombreComercial;
	private final String direccionMatriz;
	private final Boolean obligadoContabilidad;
	private final Boolean contribuyenteRimpe;

	public EmpresaModel(Long id, String ruc, String razonSocial, String nombreComercial, String direccionMatriz,
			Boolean obligadoContabilidad, Boolean contribuyenteRimpe) {

		if (ruc == null || ruc.isBlank()) {
			throw new DomainException("FACTUCORE.EMPRESA.RUC.REQUERIDO");
		}

		if (razonSocial == null || razonSocial.isBlank()) {
			throw new DomainException("FACTUCORE.EMPRESA.RAZON_SOCIAL.REQUERIDA");
		}

		if (direccionMatriz == null || direccionMatriz.isBlank()) {
			throw new DomainException("FACTUCORE.EMPRESA.DIRECCION_MATRIZ.REQUERIDA");
		}

		this.id = id;
		this.ruc = ruc;
		this.razonSocial = razonSocial;
		this.nombreComercial = nombreComercial;
		this.direccionMatriz = direccionMatriz;
		this.obligadoContabilidad = obligadoContabilidad;
		this.contribuyenteRimpe = contribuyenteRimpe;
	}

	public Long getId() {
		return id;
	}

	public String getRuc() {
		return ruc;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public String getNombreComercial() {
		return nombreComercial;
	}

	public String getDireccionMatriz() {
		return direccionMatriz;
	}

	public Boolean getObligadoContabilidad() {
		return obligadoContabilidad;
	}

	public Boolean getContribuyenteRimpe() {
		return contribuyenteRimpe;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof EmpresaModel that)) {
			return false;
		}

		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}