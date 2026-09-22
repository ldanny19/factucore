package ec.dalara.factucore.domain.documentoxsd;

import java.math.BigDecimal;

public class ElementoXsdReglaModel {

	private final String nombre;
	private final String tipoDato;
	private final Boolean obligatorio;
	private final Boolean repetible;
	private final Integer minOcurrencias;
	private final Integer maxOcurrencias;
	private final Integer longitudMinima;
	private final Integer longitudMaxima;
	private final Integer digitosTotales;
	private final Integer decimales;
	private final BigDecimal valorMinimo;
	private final BigDecimal valorMaximo;
	private final String patron;

	public ElementoXsdReglaModel(String nombre, String tipoDato, Boolean obligatorio, Boolean repetible,
			Integer minOcurrencias, Integer maxOcurrencias, Integer longitudMinima, Integer longitudMaxima,
			Integer digitosTotales, Integer decimales, BigDecimal valorMinimo, BigDecimal valorMaximo, String patron) {
		this.nombre = nombre;
		this.tipoDato = tipoDato;
		this.obligatorio = obligatorio;
		this.repetible = repetible;
		this.minOcurrencias = minOcurrencias;
		this.maxOcurrencias = maxOcurrencias;
		this.longitudMinima = longitudMinima;
		this.longitudMaxima = longitudMaxima;
		this.digitosTotales = digitosTotales;
		this.decimales = decimales;
		this.valorMinimo = valorMinimo;
		this.valorMaximo = valorMaximo;
		this.patron = patron;
	}

	public String getNombre() {
		return nombre;
	}

	public String getTipoDato() {
		return tipoDato;
	}

	public Boolean getObligatorio() {
		return obligatorio;
	}

	public Boolean getRepetible() {
		return repetible;
	}

	public Integer getMinOcurrencias() {
		return minOcurrencias;
	}

	public Integer getMaxOcurrencias() {
		return maxOcurrencias;
	}

	public Integer getLongitudMinima() {
		return longitudMinima;
	}

	public Integer getLongitudMaxima() {
		return longitudMaxima;
	}

	public Integer getDigitosTotales() {
		return digitosTotales;
	}

	public Integer getDecimales() {
		return decimales;
	}

	public BigDecimal getValorMinimo() {
		return valorMinimo;
	}

	public BigDecimal getValorMaximo() {
		return valorMaximo;
	}

	public String getPatron() {
		return patron;
	}
}