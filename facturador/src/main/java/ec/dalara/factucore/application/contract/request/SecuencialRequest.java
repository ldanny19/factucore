package ec.dalara.factucore.application.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuencialRequest {

    @NotNull
    private Long idPuntoEmision;

    @NotBlank
    @Size(max = 2)
    private String codigoDocumento;

    @NotNull
    private Long ultimoSecuencial;
}