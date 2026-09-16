package ec.dalara.factucore.application.contract.request;

import jakarta.validation.constraints.NotBlank;
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
public class ComprobanteDetalleImpuestoRequest {

    @NotNull
    private Long idComprobanteDetalle;

    @NotBlank
    @Size(max = 10)
    private String codigoImpuesto;

    @Size(max = 10)
    private String codigoPorcentaje;

    private BigDecimal tarifa;

    @NotNull
    private BigDecimal baseImponible;

    @NotNull
    private BigDecimal valor;

    private BigDecimal valorDevolucionIva;
}