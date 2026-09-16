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
public class ComprobantePagoResponse {

    private Long id;

    private Long idComprobante;

    private String codigoFormaPago;

    private BigDecimal total;

    private Integer plazo;

    private String unidadTiempo;

    private String estadoRegistro;

    private String usuarioCreacion;

    private String usuarioModificacion;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    private String observacion;
}