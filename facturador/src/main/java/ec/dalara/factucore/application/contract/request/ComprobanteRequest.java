package ec.dalara.factucore.application.contract.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class ComprobanteRequest {

	@NotNull
	private Long idEmpresa;

	@NotNull
	private Long idEstablecimiento;

	@NotNull
	private Long idPuntoEmision;

	@NotNull
	private Long idDocumentoXsd;

	@NotNull
	private Long idVersionDocumentoXsd;

	@Size(max = 2)
	private String ambiente;

	@Size(max = 2)
	private String tipoEmision;

	@NotBlank
	@Size(max = 2)
	private String codigoDocumento;

	@NotBlank
	@Size(max = 9)
	private String secuencial;

	@Size(max = 49)
	private String claveAcceso;

	@NotNull
	private LocalDate fechaEmision;

	@Size(max = 300)
	private String razonSocialEmisor;

	@Size(max = 300)
	private String nombreComercialEmisor;

	@Size(max = 13)
	private String rucEmisor;

	@Size(max = 500)
	private String direccionMatrizEmisor;

	@Size(max = 500)
	private String direccionEstablecimientoEmisor;

	@Size(max = 20)
	private String identificacionReceptor;

	@Size(max = 2)
	private String tipoIdentificacionReceptor;

	@Size(max = 300)
	private String razonSocialReceptor;

	@Size(max = 500)
	private String direccionReceptor;

	@Size(max = 50)
	private String estadoProceso;

	@Size(max = 50)
	private String codigoError;

	@Size(max = 1000)
	private String mensajeError;

	@Size(max = 100)
	private String numeroAutorizacion;

	private LocalDateTime fechaAutorizacion;

	@Size(max = 1000)
	private String rutaXmlFirmado;

	@Size(max = 1000)
	private String rutaRespuestaSri;

	@Size(max = 1000)
	private String rutaRide;
}