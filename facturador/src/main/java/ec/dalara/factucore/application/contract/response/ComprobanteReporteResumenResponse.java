package ec.dalara.factucore.application.contract.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteResumenResponse {

	long total;
	long autorizados;
	long pendientes;
	long noAutorizados;
	long conError;
	long otros;
	java.util.List<ComprobanteReporteDiaResponse> porDia;
}
