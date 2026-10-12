package ec.dalara.factucore.application.contract.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaTributariaVigenciaResponse {

	private Long id;
	private Long idCatalogoItem;
	private String codigoImpuesto;
	private String codigoPorcentaje;
	private BigDecimal tarifa;
	private LocalDate fechaVigenciaDesde;
	private LocalDate fechaVigenciaHasta;
	private String estadoRegistro;
	private String usuarioCreacion;
	private String usuarioModificacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private String observacion;
}
