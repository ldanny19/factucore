package ec.dalara.factucore.application.contract.request;

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
public class MapeoXsdRequest {

	@NotNull
	private Long idVersionDocumentoXsd;

	@NotBlank
	@Size(max = 1000)
	private String rutaOrigen;

	private Long idElementoXsd;

	private Long idAtributoXsd;

	@NotBlank
	@Size(max = 20)
	private String tipoMapeo;
}
