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
public class ComprobanteAuditoriaResponse {

	private Long id;

	private Long idComprobante;

	private String estadoAnterior;

	private String estadoNuevo;

	private String codigoError;

	private String mensajeError;

	private String estadoRegistro;

	private String usuarioCreacion;

	private String usuarioModificacion;

	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaModificacion;

	private String observacion;
}