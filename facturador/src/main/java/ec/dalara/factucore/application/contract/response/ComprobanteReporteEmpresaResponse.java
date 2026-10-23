package ec.dalara.factucore.application.contract.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteEmpresaResponse {

	Long id;
	String ruc;
	String razonSocial;
}
