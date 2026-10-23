package ec.dalara.factucore.application.contract.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComprobanteReporteAuditoriaResponse {

	String etapa;
	String resultado;
	String estadoAnterior;
	String estadoNuevo;
	String codigoError;
	String mensajeError;
	LocalDateTime fechaInicio;
	LocalDateTime fechaFin;
	Integer intento;
}
