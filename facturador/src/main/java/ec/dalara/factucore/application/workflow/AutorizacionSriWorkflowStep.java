package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ConfiguracionEmpresaService;
import ec.dalara.factucore.application.service.SriCodigoCatalogoService;
import ec.dalara.factucore.application.service.SriService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutorizacionSriWorkflowStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutorizacionSriWorkflowStep.class);

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final SriService sriService;
	private final SriProperties sriProperties;
	private final MessageResolver messageResolver;
	private final SriCodigoCatalogoService sriCodigoCatalogoService;
	private final ConfiguracionEmpresaService configuracionEmpresaService;
	private final ComprobanteAuditoriaService comprobanteAuditoriaService;

	private static final String CONFIG_TIEMPO_MAX_COD_70 = "TIEMPO_MAX_COD_70";

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.AUTORIZACION_SRI;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private void registrarRespuestaSri(ContextoWorkflow contexto, SriResponse respuesta) {
		if (!LOGGER.isDebugEnabled()) {
			return;
		}

		String idTransaccion = contexto.getSolicitud() == null ? null : contexto.getSolicitud().getIdTransaccion();
		LOGGER.debug(messageResolver.resolver(MessageCodes.LOG_SRI_RESPUESTA_COMPLETA, idTransaccion,
				etapa().name(), respuesta.respuestaXml()));
	}

	private void validarCodigosSri(ContextoWorkflow contexto, SriResponse respuesta) {
		String idTransaccion = contexto.getSolicitud() == null ? null : contexto.getSolicitud().getIdTransaccion();
		respuesta.mensajes().stream().map(mensaje -> mensaje.identificador()).filter(codigo -> codigo != null && !codigo.isBlank())
				.filter(codigo -> !sriCodigoCatalogoService.esCodigoConocido(codigo))
				.forEach(codigo -> LOGGER.warn(messageResolver.resolver(MessageCodes.LOG_SRI_CODIGO_NO_PARAMETRIZADO, idTransaccion, etapa().name(), codigo)));
	}

	private String obtenerUsuario(ContextoWorkflow contexto) {
		if (contexto.getSolicitud() != null && contexto.getSolicitud().getUsuario() != null
				&& !contexto.getSolicitud().getUsuario().isBlank()) {
			return contexto.getSolicitud().getUsuario();
		}
		String usuario = contexto.getComprobante().getUsuarioModificacion();
		return usuario == null || usuario.isBlank() ? contexto.getComprobante().getUsuarioCreacion() : usuario;
	}

	private boolean respuestaEnProceso(SriResponse respuesta) {
		return "EN PROCESO".equalsIgnoreCase(respuesta.estado())
				|| respuesta.mensajes().stream().anyMatch(mensaje -> "70".equals(mensaje.identificador()));
	}

	private boolean respuestaCodigo70(SriResponse respuesta) {
		return respuesta.mensajes().stream().anyMatch(mensaje -> "70".equals(mensaje.identificador()));
	}

	private Long obtenerTiempoMaximoCodigo70Segundos(ContextoWorkflow contexto) {
		Long empresaId = contexto.getComprobante().getEmpresa() == null
				? null : contexto.getComprobante().getEmpresa().getId();
		if (empresaId == null) {
			return null;
		}

		var configuracion = configuracionEmpresaService.obtenerVigente(empresaId, CONFIG_TIEMPO_MAX_COD_70,
				LocalDateTime.now());
		if (configuracion.isEmpty() || configuracion.get().getValor() == null
				|| configuracion.get().getValor().isBlank()) {
			return null;
		}

		try {
			long segundos = Long.parseLong(configuracion.get().getValor().trim());
			return segundos > 0 ? segundos : null;
		} catch (NumberFormatException exception) {
			LOGGER.warn("El valor de {} no es un número de segundos válido para la empresa {}.",
					CONFIG_TIEMPO_MAX_COD_70, empresaId);
			return null;
		}
	}

	private void programarReconsultaCodigo70(ContextoWorkflow contexto, LocalDateTime fechaPrimerEnvio,
			long tiempoMaximoSegundos) {
		LocalDateTime limite = fechaPrimerEnvio.plusSeconds(tiempoMaximoSegundos);
		LocalDateTime ahora = LocalDateTime.now();
		if (!ahora.isBefore(limite)) {
			contexto.getComprobante().setFechaProximoReproceso(null);
			return;
		}

		long intervaloMs = Math.max(sriProperties.getAutorizacion().getEsperaReintentoMs(), 1L);
		LocalDateTime siguiente = ahora.plusNanos(intervaloMs * 1_000_000L);
		contexto.getComprobante().setFechaProximoReproceso(siguiente.isAfter(limite) ? limite : siguiente);
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
		}

		if (EstadoProceso.ENVIADO_SRI.name().equals(contexto.getComprobante().getEstadoProceso())) {
			esperar(sriProperties.getAutorizacion().getEsperaInicialMs());
		}

		int maxIntentos = Math.max(sriProperties.getAutorizacion().getMaxIntentos(), 1);
		Long tiempoMaximoCodigo70Segundos = obtenerTiempoMaximoCodigo70Segundos(contexto);
		LocalDateTime fechaPrimerEnvio = tiempoMaximoCodigo70Segundos == null ? null
				: comprobanteAuditoriaService.obtenerFechaPrimerEnvio(contexto.getComprobanteId());
		boolean politicaTiempoCodigo70 = tiempoMaximoCodigo70Segundos != null && fechaPrimerEnvio != null;
		SriResponse respuesta = null;
		int intento = 0;
		do {
			intento++;
			respuesta = sriService.autorizar(contexto.getClaveAcceso());
			contexto.setEstadoSri(respuesta.estado());
			contexto.setMensajesSri(respuesta.mensajes());
			registrarRespuestaSri(contexto, respuesta);
			validarCodigosSri(contexto, respuesta);

			if ((politicaTiempoCodigo70 && respuestaCodigo70(respuesta))
					|| !respuestaEnProceso(respuesta) || intento >= maxIntentos) {
				break;
			}

			esperar(sriProperties.getAutorizacion().getEsperaReintentoMs());
		} while (intento < maxIntentos);

		if ("AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
			contexto.setNumeroAutorizacion(respuesta.identificador());
			contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZACION_SRI.name());
			contexto.getComprobante().setEstadoSri("AUTORIZADO");
			contexto.getComprobante().setFechaProximoReproceso(null);
			contexto.getComprobante().setNumeroAutorizacion(respuesta.identificador());
			contexto.setFechaAutorizacion(respuesta.fechaAutorizacion());
			contexto.getComprobante().setFechaAutorizacion(respuesta.fechaAutorizacion());
			evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta,
					obtenerUsuario(contexto));
			return ResultadoEtapa.exitosa(etapa(), "AUTORIZADO",
					Map.of("numeroAutorizacion", respuesta.identificador() == null ? "" : respuesta.identificador()));
		}

		if (respuestaEnProceso(respuesta)) {
			contexto.getComprobante().setEstadoSri(respuesta.estado());
			contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZACION_PENDIENTE.name());
			if (politicaTiempoCodigo70 && respuestaCodigo70(respuesta)) {
				programarReconsultaCodigo70(contexto, fechaPrimerEnvio, tiempoMaximoCodigo70Segundos);
			} else {
				contexto.getComprobante().setFechaProximoReproceso(null);
			}

			var ultimoMensaje = respuesta.mensajes().stream()
					.filter(mensaje -> "70".equals(mensaje.identificador())).findFirst()
					.orElse(respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0));
			String codigoError = ultimoMensaje == null || ultimoMensaje.identificador() == null
					|| ultimoMensaje.identificador().isBlank()
					? MessageCodes.SRI_AUTORIZACION_MAX_CONSULTAS
					: ultimoMensaje.identificador();
			String mensajeError = ultimoMensaje == null || ultimoMensaje.mensaje() == null
					|| ultimoMensaje.mensaje().isBlank()
					? messageResolver.resolver(MessageCodes.SRI_AUTORIZACION_MAX_CONSULTAS)
					: sriCodigoCatalogoService.resolverDescripcion(codigoError, ultimoMensaje.mensaje());
			contexto.getComprobante().setCodigoError(codigoError);
			contexto.getComprobante().setMensajeError(mensajeError);
			evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta,
					contexto.getSolicitud().getUsuario());
			return ResultadoEtapa.fallida(etapa(), EstadoProceso.AUTORIZACION_PENDIENTE.name(),
					codigoError, mensajeError);
		}

		contexto.getComprobante().setEstadoSri(respuesta.estado() == null || respuesta.estado().isBlank()
				? "NO_AUTORIZADO" : respuesta.estado());
		contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZACION_SRI.name());
		contexto.getComprobante().setFechaProximoReproceso(null);
		evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta,
				contexto.getSolicitud().getUsuario());
		var mensaje = respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0);
		return ResultadoEtapa.fallida(etapa(), "NO_AUTORIZADO",
				mensaje == null || mensaje.identificador() == null ? MessageCodes.SRI_RESPUESTA_INVALIDA
						: mensaje.identificador(),
				mensaje == null ? null : sriCodigoCatalogoService.resolverDescripcion(mensaje.identificador(), mensaje.mensaje()));
	}

	private void esperar(long esperaMs) {
		if (esperaMs <= 0)
			return;
		try {
			Thread.sleep(esperaMs);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION, exception);
		}
	}
}
