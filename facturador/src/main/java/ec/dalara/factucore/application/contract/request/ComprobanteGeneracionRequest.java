package ec.dalara.factucore.application.contract.request;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
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
public class ComprobanteGeneracionRequest {

	@NotBlank
	@Size(max = 100)
	private String idTransaccion;

	@NotNull
	private OffsetDateTime fechaInicio;

	@NotBlank
	private String usuario;

	@NotBlank
	private String canal;

	@NotNull
	private Long idEmpresa;

	@NotBlank
	private String codigoEstablecimiento;

	@NotBlank
	private String puntoEmision;

	@NotBlank
	private String tipoDocumento;

	@Valid
	private List<DatoComprobanteRequest> datos;
}