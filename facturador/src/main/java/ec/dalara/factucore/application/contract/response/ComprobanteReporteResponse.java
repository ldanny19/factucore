package ec.dalara.factucore.application.contract.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteResponse {

	Long id;
	String idTransaccion;
	Long idEmpresa;
	String razonSocialEmisor;
	String rucEmisor;
	Long idEstablecimiento;
	Long idPuntoEmision;
	String codigoDocumento;
	String tipoDocumento;
	String numeroComprobante;
	String claveAcceso;
	LocalDate fechaEmision;
	String identificacionReceptor;
	String razonSocialReceptor;
	String estadoProceso;
	String estadoSri;
	String numeroAutorizacion;
	LocalDateTime fechaAutorizacion;
	String codigoError;
	String mensajeError;
}
