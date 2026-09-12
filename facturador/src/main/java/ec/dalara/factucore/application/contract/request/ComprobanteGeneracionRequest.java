package ec.dalara.factucore.application.contract.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteGeneracionRequest {

    @NotBlank
    private String idTransaccion;

    @NotNull
    private OffsetDateTime fechaInicio;

    @NotBlank
    private String usuario;

    @NotBlank
    private String canal;

    @NotNull
    private Long idEmpresa;

    @NotNull
    private Long idEstablecimiento;

    @NotBlank
    private String puntoEmision;

    @NotBlank
    private String tipoDocumento;

    @Valid
    private List<DatoComprobanteRequest> datos;
}