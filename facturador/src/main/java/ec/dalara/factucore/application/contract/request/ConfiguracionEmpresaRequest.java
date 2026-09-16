package ec.dalara.factucore.application.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionEmpresaRequest {

    @NotNull
    private Long idEmpresa;

    @NotBlank
    @Size(max = 100)
    private String clave;

    @Size(max = 2000)
    private String valor;

    @NotBlank
    @Size(max = 30)
    private String tipoDato;

    @NotNull
    private LocalDateTime fechaVigenciaDesde;

    private LocalDateTime fechaVigenciaHasta;
}