package ec.dalara.factucore.domain.comprobante;

import java.math.BigDecimal;

public class ComprobantePagoModel {

	private String codigoFormaPago;
	private BigDecimal total;
	private Integer plazo;
	private String unidadTiempo;

	public ComprobantePagoModel(String codigoFormaPago, BigDecimal total, Integer plazo, String unidadTiempo) {
		this.codigoFormaPago = codigoFormaPago;
		this.total = total;
		this.plazo = plazo;
		this.unidadTiempo = unidadTiempo;
	}

	public String getCodigoFormaPago() {
		return codigoFormaPago;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public Integer getPlazo() {
		return plazo;
	}

	public String getUnidadTiempo() {
		return unidadTiempo;
	}
}