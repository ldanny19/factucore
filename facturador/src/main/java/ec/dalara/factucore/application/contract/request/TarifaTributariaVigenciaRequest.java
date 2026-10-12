package ec.dalara.factucore.application.contract.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class TarifaTributariaVigenciaRequest {

	@NotNull
	private Long idCatalogoItem;

	@NotBlank
	@Size(max = 2)
	private String codigoImpuesto;

	@NotBlank
	@Size(max = 4)
	private String codigoPorcentaje;

	@NotNull
	@DecimalMin("0.0")
	private BigDecimal tarifa;

	@NotNull
	private LocalDate fechaVigenciaDesde;

	private LocalDate fechaVigenciaHasta;

	@Size(max = 500)
	private String observacion;
}
