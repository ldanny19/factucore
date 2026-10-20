package ec.dalara.factucore.application.contract.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@JsonIgnore
	private String idTransaccion;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime fechaInicio;

	@NotBlank
	private String canal;

	/*
	 * El contexto de empresa puede ser nulo al administrar la propia entidad
	 * Empresa, que es la raíz del agregado. Los recursos hijos validan su
	 * idEmpresa dentro de sus respectivos contratos de datos.
	 */
	private Long idEmpresa;

	@Valid
	@NotNull
	private T datos;
}
