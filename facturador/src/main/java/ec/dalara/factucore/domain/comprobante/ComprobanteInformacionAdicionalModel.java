package ec.dalara.factucore.domain.comprobante;

import java.util.Objects;

public class ComprobanteInformacionAdicionalModel {

	private String nombre;
	private String valor;

	public ComprobanteInformacionAdicionalModel(String nombre, String valor) {
		this.nombre = Objects.requireNonNull(nombre);
		this.valor = valor;
	}

	public String getNombre() {
		return nombre;
	}

	public String getValor() {
		return valor;
	}
}