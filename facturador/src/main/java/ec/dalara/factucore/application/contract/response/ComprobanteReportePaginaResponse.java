package ec.dalara.factucore.application.contract.response;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReportePaginaResponse {

	List<ComprobanteReporteResponse> resultados;
	int pagina;
	int tamano;
	long totalElementos;
	int totalPaginas;
}
