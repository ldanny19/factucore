package ec.dalara.factucore.application.contract.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministracionRequest<T> {

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

    @Valid
    @NotNull
    private T datos;
}