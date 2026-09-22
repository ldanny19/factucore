package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapeoXsdResponse {

	private Long id;

	private Long idVersionDocumentoXsd;

	private String rutaOrigen;

	private Long idElementoXsd;

	private Long idAtributoXsd;

	private String tipoMapeo;

	private String estadoRegistro;
}
