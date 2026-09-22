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
public class EmpresaResponse {

	private Long id;

	private String ruc;

	private String razonSocial;

	private String nombreComercial;

	private String direccionMatriz;

	private Boolean obligadoContabilidad;

	private Boolean contribuyenteRimpe;

	private String estadoRegistro;

	private String usuarioCreacion;

	private String usuarioModificacion;

	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaModificacion;

	private String observacion;
}