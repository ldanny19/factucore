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
public class AtributoXsdRequest {

	@NotNull
	private Long idElementoXsd;

	@NotBlank
	@Size(max = 300)
	private String nombre;

	@Size(max = 100)
	private String tipoDato;

	@NotNull
	private Boolean obligatorio;

	@Size(max = 1000)
	private String valorPredeterminado;

	@Size(max = 2000)
	private String patron;
}