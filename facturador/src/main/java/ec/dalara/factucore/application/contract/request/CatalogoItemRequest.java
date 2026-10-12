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
public class CatalogoItemRequest {

	@NotNull
	private Long idCatalogo;

	@NotBlank
	@Size(max = 100)
	private String codigo;

	@NotBlank
	@Size(max = 300)
	private String nombre;

	@Size(max = 500)
	private String descripcion;

	private Integer orden;
}