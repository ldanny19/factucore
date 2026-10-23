package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampoFuenteMapeoXsdResponse {

	private String codigo;
	private String nombre;
	private String tipoDato;
	private String origen;
}
