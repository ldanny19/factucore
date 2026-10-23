package ec.dalara.factucore.application.contract.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteDiaResponse {

	LocalDate fecha;
	long total;
	long autorizados;
	long pendientes;
	long noAutorizados;
	long conError;
	long otros;
}
