package ec.dalara.factucore.application.contract.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDetalleRequest {

	@NotNull
	private Long idComprobante;

	private Integer orden;

	@Size(max = 300)
	private String codigoPrincipal;

	@Size(max = 300)
	private String codigoAuxiliar;

	@Size(max = 500)
	private String descripcion;

	@NotNull
	private BigDecimal cantidad;

	@NotNull
	private BigDecimal precioUnitario;

	private BigDecimal descuento;

	@NotNull
	private BigDecimal precioTotalSinImpuesto;
}