package ec.dalara.factucore.application.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.MensajeResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.application.port.out.sri.SriMensaje;
import ec.dalara.factucore.application.service.SriCodigoCatalogoService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.persistence.repository.EstablecimientoRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.PuntoEmisionRepository;
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
	private final SriCodigoCatalogoService sriCodigoCatalogoService;
	private final EstablecimientoRepository establecimientoRepository;
	private final PuntoEmisionRepository puntoEmisionRepository;

	private String obtenerNumeroComprobante(ec.dalara.factucore.infrastructure.persistence.entity.Comprobante comprobante) {
		if (comprobante == null || comprobante.getEstablecimiento() == null || comprobante.getPuntoEmision() == null) {
			return null;
		}

		Long establecimientoId = comprobante.getEstablecimiento().getId();
		Long puntoEmisionId = comprobante.getPuntoEmision().getId();
		if (establecimientoId == null || puntoEmisionId == null) {
			return null;
		}

		var establecimiento = establecimientoRepository.findById(establecimientoId).orElse(null);
		var puntoEmision = puntoEmisionRepository.findById(puntoEmisionId).orElse(null);
		if (establecimiento == null || puntoEmision == null) {
			return null;
		}

		return comprobante.getCodigoDocumento() + "-" + establecimiento.getCodigo() + "-"
				+ puntoEmision.getCodigo() + "-" + comprobante.getSecuencial();
	}

	public ComprobanteGeneracionResponse crear(ContextoWorkflow contexto) {
		var comprobante = contexto == null ? null : contexto.getComprobante();
		var request = contexto == null ? null : contexto.getSolicitud();

		String numeroComprobante = obtenerNumeroComprobante(comprobante);

		var ultimo = contexto == null ? null : contexto.getUltimoResultado();
		boolean exitoso = contexto != null && Boolean.TRUE.equals(contexto.getExitosoFinal());
		String codigo = ultimo == null || ultimo.isExitosa() ? null : ultimo.getCodigoError();
		String mensaje = ultimo == null || ultimo.isExitosa() ? null : ultimo.getMensaje();

		if (contexto != null && contexto.isIdempotente()
				&& "AUTORIZADO".equalsIgnoreCase(contexto.getEstadoSri())) {
			codigo = MessageCodes.COMPROBANTE_YA_AUTORIZADO;
			mensaje = messageResolver.resolver(codigo, contexto.getNumeroAutorizacion());
		}

		List<MensajeResponse> errores = contexto == null ? List.of() : contexto.getErroresValidacion();
		List<MensajeResponse> advertencias = List.of();
		List<MensajeResponse> mensajesInformativos = List.of();
		List<MensajeResponse> erroresSri = new ArrayList<>();
		List<MensajeResponse> advertenciasSri = new ArrayList<>();
		List<MensajeResponse> mensajesSri = new ArrayList<>();

		if (contexto != null && !contexto.getMensajesSri().isEmpty()) {
			for (SriMensaje mensajeSri : contexto.getMensajesSri()) {
				MensajeResponse mensajeResponse = MensajeResponse.builder()
						.codigo(mensajeSri.identificador())
						.mensaje(sriCodigoCatalogoService.resolverDescripcion(mensajeSri.identificador(), mensajeSri.mensaje()))
						.campo(null)
						.informacionAdicional(mensajeSri.informacionAdicional())
						.build();

				String tipo = mensajeSri.tipo() == null ? "" : mensajeSri.tipo().trim().toUpperCase(Locale.ROOT);
				switch (tipo) {
				case "ERROR" -> erroresSri.add(mensajeResponse);
				case "ADVERTENCIA", "WARNING" -> advertenciasSri.add(mensajeResponse);
				default -> mensajesSri.add(mensajeResponse);
				}
			}
			errores = erroresSri;
			advertencias = advertenciasSri;
			mensajesInformativos = mensajesSri;
		}

		List<MensajeResponse> validacionMensajes = List.of();
		if (contexto != null && contexto.getValidacionXsd() != null) {
			errores = contexto.getValidacionXsd().getErrores();
			advertencias = contexto.getValidacionXsd().getAdvertencias();
			validacionMensajes = contexto.getValidacionXsd().getMensajes();
		}

		if ((contexto == null || contexto.getMensajesSri().isEmpty())
				&& errores.isEmpty() && ultimo != null && !ultimo.isExitosa()) {
			errores = List.of(MensajeResponse.builder().codigo(ultimo.getCodigoError()).mensaje(ultimo.getMensaje()).build());
		}
		if (!exitoso && (codigo == null || mensaje == null) && !errores.isEmpty()) {
			codigo = errores.get(0).getCodigo();
			mensaje = errores.get(0).getMensaje();
		}

		List<MensajeResponse> mensajesNotificacion = contexto != null && contexto.isErrorNotificacion()
				? List.of(MensajeResponse.builder()
						.codigo(MessageCodes.NOTIFICACION_PUBLICACION_ERROR)
						.mensaje(messageResolver.resolver(MessageCodes.NOTIFICACION_PUBLICACION_ERROR))
						.build())
				: List.of();

		ResultadoResponse resultado = ResultadoResponse.builder()
				.errores(errores)
				.advertencias(advertencias)
				.mensajes(!validacionMensajes.isEmpty() ? validacionMensajes
						: (!mensajesInformativos.isEmpty() ? mensajesInformativos : mensajesNotificacion))
				.build();

		return ComprobanteGeneracionResponse.builder()
				.idTransaccion(request == null ? null : request.getIdTransaccion())
				.idComprobante(contexto == null ? null : contexto.getComprobanteId())
				.numeroAutorizacion(contexto == null ? null : contexto.getNumeroAutorizacion())
				.fechaInicio(contexto == null || contexto.getFechaInicio() == null ? null
						: contexto.getFechaInicio().atOffset(ZoneOffset.UTC))
				.fechaFin(OffsetDateTime.now(ZoneOffset.UTC))
				.exitoso(exitoso)
				.codigo(codigo)
				.mensaje(mensaje)
				.resultado(resultado)
				.claveAcceso(contexto == null ? null : contexto.getClaveAcceso())
				.numeroComprobante(numeroComprobante)
				.tipoDocumento(comprobante == null ? null : comprobante.getCodigoDocumento())
				.estadoSri(contexto == null ? null : contexto.getEstadoSri())
				.archivoPdf(contexto == null ? null : contexto.getRide())
				.build();
	}
}
