package ec.dalara.factucore.application.contract.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRetencionResponse {

	private Long id;

	private Long idComprobante;

	private String codigoImpuesto;

	private String codigoRetencion;

	private BigDecimal porcentajeRetener;

	private BigDecimal baseImponible;

	private BigDecimal valorRetenido;

	private String numeroDocumentoSustento;

	private LocalDate fechaEmisionDocumentoSustento;

	private String estadoRegistro;

	private String usuarioCreacion;

	private String usuarioModificacion;

	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaModificacion;

	private String observacion;
}