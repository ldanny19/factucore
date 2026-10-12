package ec.dalara.factucore.application.contract.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoXsdResponse {

	private Long id;
	private Long idElementoXsd;
	private String nombre;
	private String tipoDato;
	private Boolean obligatorio;
	private String valorPredeterminado;
	private String patron;
	private String estadoRegistro;
	private String usuarioCreacion;
	private String usuarioModificacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private String observacion;
}