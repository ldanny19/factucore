package ec.dalara.factucore.application.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.MensajeResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import lombok.RequiredArgsConstructor;

/**
 * Construye la respuesta pública del proceso a partir del contexto final.
 *
 * <p>La fábrica no decide transiciones ni estados del workflow. Solo transforma
 * el resultado que ya fue preparado por los nodos {@code GENERAR_RESPUESTA} o
 * {@code RESPUESTA_ERROR_NOTIFICACION} en el contrato REST.</p>
 */
@Component
@RequiredArgsConstructor
public class ComprobanteWorkflowResponseFactory {

	private final MessageResolver messageResolver;

	public ComprobanteGeneracionResponse crear(ContextoWorkflow contexto) {
		var comprobante = contexto == null ? null : contexto.getComprobante();
		var request = contexto == null ? null : contexto.getSolicitud();

		String numeroComprobante = comprobante == null ? null
				: comprobante.getCodigoDocumento() + "-" + comprobante.getEstablecimiento().getCodigo() + "-"
						+ comprobante.getPuntoEmision().getCodigo() + "-" + comprobante.getSecuencial();

		var ultimo = contexto == null ? null : contexto.getUltimoResultado();
		boolean exitoso = contexto != null && Boolean.TRUE.equals(contexto.getExitosoFinal());

		List<MensajeResponse> errores = contexto == null ? List.of() : contexto.getErroresValidacion();
		if (errores.isEmpty() && ultimo != null && !ultimo.isExitosa()) {
			errores = List.of(MensajeResponse.builder().codigo(ultimo.getCodigoError()).mensaje(ultimo.getMensaje()).build());
		}

		List<MensajeResponse> mensajes = contexto != null && contexto.isErrorNotificacion()
				? List.of(MensajeResponse.builder()
						.codigo(MessageCodes.NOTIFICACION_PUBLICACION_ERROR)
						.mensaje(messageResolver.resolver(MessageCodes.NOTIFICACION_PUBLICACION_ERROR))
						.build())
				: List.of();

		ResultadoResponse resultado = ResultadoResponse.builder()
				.errores(errores)
				.advertencias(List.of())
				.mensajes(mensajes)
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
