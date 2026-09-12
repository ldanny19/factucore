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
public class EmpresaRequest {

    @NotBlank
    @Size(max = 13)
    private String ruc;

    @NotBlank
    @Size(max = 300)
    private String razonSocial;

    @Size(max = 300)
    private String nombreComercial;

    @NotBlank
    @Size(max = 500)
    private String direccionMatriz;

    @NotNull
    private Boolean obligadoContabilidad;

    @NotNull
    private Boolean contribuyenteRimpe;
}