package ec.dalara.factucore.application.contract.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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