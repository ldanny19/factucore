package ec.dalara.factucore.application.contract.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuenteMapeoXsdResponse {

	private String codigo;
	private String nombre;
	private boolean requiereCatalogo;
	private List<CampoFuenteMapeoXsdResponse> campos;
}
