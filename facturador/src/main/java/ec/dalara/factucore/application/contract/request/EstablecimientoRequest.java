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
public class EstablecimientoRequest {

	@NotNull
	private Long idEmpresa;

	@NotBlank
	@Size(max = 3)
	private String codigo;

	@Size(max = 300)
	private String nombre;

	@NotBlank
	@Size(max = 500)
	private String direccion;
}