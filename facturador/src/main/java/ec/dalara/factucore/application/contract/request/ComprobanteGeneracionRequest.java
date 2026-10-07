package ec.dalara.factucore.application.contract.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime fechaInicio;

	@NotBlank
	@Size(max = 50)
	private String versionXsd;

	@NotBlank
	private String usuario;

	@NotBlank
	private String canal;

	@NotNull
	private Long idEmpresa;

	@NotNull
	private Long idEstablecimiento;

	@NotNull
	private Long idPuntoEmision;

	@NotNull
	private Long idTipoDocumento;

	private JsonNode datos;
}
