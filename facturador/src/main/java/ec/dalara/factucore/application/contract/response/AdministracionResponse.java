package ec.dalara.factucore.application.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministracionResponse<T> {

    private String idTransaccion;

    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaFin;

    private String estado;

    private String codigo;

    private String mensaje;

    private T datos;
}