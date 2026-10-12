package ec.dalara.factucore.application.contract.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDetalleImpuestoResponse {

	private Long id;
	private Long idComprobanteDetalle;
	private String codigoImpuesto;
	private String codigoPorcentaje;
	private BigDecimal tarifa;
	private BigDecimal baseImponible;
	private BigDecimal valor;
	private BigDecimal valorDevolucionIva;
	private String estadoRegistro;
	private String usuarioCreacion;
	private String usuarioModificacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private String observacion;
}