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
public class CertificadoFirmaRequest {

    @NotNull
    private Long idEmpresa;

    @Size(max = 300)
    private String nombreArchivo;

    @NotBlank
    @Size(max = 1000)
    private String rutaCertificado;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;
}