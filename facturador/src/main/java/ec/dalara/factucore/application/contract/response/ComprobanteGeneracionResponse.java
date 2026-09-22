package ec.dalara.factucore.application.contract.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteGeneracionResponse {

	private String idTransaccion;

	private OffsetDateTime fechaInicio;

	private OffsetDateTime fechaFin;

	private Boolean exitoso;

	private String estado;

	private ResultadoResponse resultado;

	private String claveAcceso;

	private String numeroComprobante;

	private String tipoDocumento;

	private String estadoSri;

	private byte[] archivoPdf;
}