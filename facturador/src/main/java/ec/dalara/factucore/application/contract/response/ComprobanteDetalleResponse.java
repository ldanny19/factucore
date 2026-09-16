package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDetalleResponse {

    private Long id;

    private Long idComprobante;

    private Integer orden;

    private String codigoPrincipal;

    private String codigoAuxiliar;

    private String descripcion;

    private BigDecimal cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal descuento;

    private BigDecimal precioTotalSinImpuesto;

    private String estadoRegistro;

    private String usuarioCreacion;

    private String usuarioModificacion;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    private String observacion;
}