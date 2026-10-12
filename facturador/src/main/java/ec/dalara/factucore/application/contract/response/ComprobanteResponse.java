package ec.dalara.factucore.application.contract.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResponse {

	private Long id;
	private Long idEmpresa;
	private Long idEstablecimiento;
	private Long idPuntoEmision;
	private Long idDocumentoXsd;
	private Long idVersionDocumentoXsd;
	private String ambiente;
	private String tipoEmision;
	private String codigoDocumento;
	private String secuencial;
	private String claveAcceso;
	private LocalDate fechaEmision;
	private String razonSocialEmisor;
	private String nombreComercialEmisor;
	private String rucEmisor;
	private String direccionMatrizEmisor;
	private String direccionEstablecimientoEmisor;
	private String identificacionReceptor;
	private String tipoIdentificacionReceptor;
	private String razonSocialReceptor;
	private String direccionReceptor;
	private String estadoProceso;
	private String codigoError;
	private String mensajeError;
	private String numeroAutorizacion;
	private LocalDateTime fechaAutorizacion;
	private String rutaXmlFirmado;
	private String rutaRespuestaSri;
	private String rutaRide;
	private String estadoRegistro;
	private String usuarioCreacion;
	private String usuarioModificacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private String observacion;
}