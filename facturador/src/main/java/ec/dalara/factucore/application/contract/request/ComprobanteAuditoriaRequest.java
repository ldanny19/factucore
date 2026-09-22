package ec.dalara.factucore.application.contract.request;

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
public class ComprobanteAuditoriaRequest {

	@NotNull
	private Long idComprobante;

	@Size(max = 50)
	private String estadoAnterior;

	@Size(max = 50)
	private String estadoNuevo;

	@Size(max = 100)
	private String codigoError;

	@Size(max = 2000)
	private String mensajeError;
}