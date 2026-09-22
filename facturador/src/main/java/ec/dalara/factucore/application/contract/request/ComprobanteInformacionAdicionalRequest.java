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
public class ComprobanteInformacionAdicionalRequest {

	@NotNull
	private Long idComprobante;

	@NotNull
	@Size(max = 300)
	private String nombre;

	@Size(max = 4000)
	private String valor;
}