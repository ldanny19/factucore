package ec.dalara.factucore.application.contract.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePagoRequest {

    @NotNull
    private Long idComprobante;

    @Size(max = 20)
    private String codigoFormaPago;

    private BigDecimal total;

    private Integer plazo;

    @Size(max = 20)
    private String unidadTiempo;
}