package ec.dalara.factucore.application.contract.request;

import java.time.OffsetDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministracionRequest<T> {

	@NotBlank
	private String idTransaccion;

	@NotNull
	private OffsetDateTime fechaInicio;

	@NotBlank
	private String usuario;

	@NotBlank
	private String canal;

	@NotNull
	private Long idEmpresa;

	@Valid
	@NotNull
	private T datos;
}