package ec.dalara.factucore.application.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.MensajeResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;

@Component
public class ComprobanteWorkflowResponseFactory {

	public ComprobanteGeneracionResponse crear(ContextoWorkflow contexto) {
		var comprobante = contexto == null ? null : contexto.getComprobante();
		var request = contexto == null ? null : contexto.getSolicitud();

		String numeroComprobante = comprobante == null ? null
				: comprobante.getCodigoDocumento() + "-" + comprobante.getEstablecimiento().getCodigo() + "-"
						+ comprobante.getPuntoEmision().getCodigo() + "-" + comprobante.getSecuencial();

		boolean exitoso = comprobante != null
				&& (EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())
						|| EstadoProceso.AUTORIZADO.name().equals(comprobante.getEstadoProceso()));

		var ultimo = contexto == null ? null : contexto.getUltimoResultado();

		ResultadoResponse resultado = ResultadoResponse.builder()
				.errores(ultimo != null && !ultimo.isExitosa()
						? java.util.List.of(MensajeResponse.builder().codigo(ultimo.getCodigoError())
								.mensaje(ultimo.getMensaje()).build())
						: java.util.List.of())
				.advertencias(java.util.List.of())
				.mensajes(java.util.List.of())
				.build();

		return ComprobanteGeneracionResponse.builder()
				.idTransaccion(request == null ? null : request.getIdTransaccion())
				.fechaInicio(contexto == null || contexto.getFechaInicio() == null ? null
						: contexto.getFechaInicio().atOffset(ZoneOffset.UTC))
				.fechaFin(OffsetDateTime.now(ZoneOffset.UTC))
				.exitoso(exitoso)
				.resultado(resultado)
				.claveAcceso(contexto == null ? null : contexto.getClaveAcceso())
				.numeroComprobante(numeroComprobante)
				.tipoDocumento(comprobante == null ? null : comprobante.getCodigoDocumento())
				.estadoSri(contexto == null ? null : contexto.getEstadoSri())
				.archivoPdf(contexto == null ? null : contexto.getRide())
				.build();
	}
}
