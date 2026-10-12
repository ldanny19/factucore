package ec.dalara.factucore.application.contract.request;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class ComprobanteRetencionRequest {

	@NotNull
	private Long idComprobante;

	@Size(max = 20)
	private String codigoImpuesto;

	@Size(max = 20)
	private String codigoRetencion;

	private BigDecimal porcentajeRetener;

	private BigDecimal baseImponible;

	private BigDecimal valorRetenido;

	@Size(max = 50)
	private String numeroDocumentoSustento;

	private LocalDate fechaEmisionDocumentoSustento;
}