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
public class CertificadoFirmaResponse {

	private Long id;
	private Long idEmpresa;
	private String nombreArchivo;
	private String rutaCertificado;
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;
	private String estadoRegistro;
	private String usuarioCreacion;
	private String usuarioModificacion;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private String observacion;
}