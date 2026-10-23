package ec.dalara.factucore.application.contract.response;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteDetalleResponse {

	ComprobanteReporteResponse comprobante;
	List<ComprobanteReporteAuditoriaResponse> auditoria;
}
