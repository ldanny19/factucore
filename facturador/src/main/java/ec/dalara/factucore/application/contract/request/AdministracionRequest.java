package ec.dalara.factucore.application.contract.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime fechaInicio;

	@NotBlank
	private String canal;

	@NotNull
	private Long idEmpresa;

	@Valid
	@NotNull
	private T datos;
}
