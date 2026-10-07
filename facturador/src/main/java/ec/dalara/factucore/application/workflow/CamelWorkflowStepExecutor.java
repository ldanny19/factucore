package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteReprocessService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component("camelWorkflowStepExecutor")
@RequiredArgsConstructor
public class CamelWorkflowStepExecutor {

	private final ValidacionComprobanteWorkflowStep validacion;
	private final AsignacionSecuencialWorkflowStep asignacionSecuencial;
	private final GeneracionClaveAccesoWorkflowStep generacionClaveAcceso;
	private final PersistenciaComprobanteWorkflowStep persistenciaComprobante;
	private final GeneracionXmlWorkflowStep generacionXml;
	private final ValidacionXsdWorkflowStep validacionXsd;
	private final FirmaElectronicaWorkflowStep firmaElectronica;
	private final EnvioSriWorkflowStep envioSri;
	private final AutorizacionSriWorkflowStep autorizacionSri;
	private final GeneracionRideWorkflowStep generacionRide;
	private final NotificacionWorkflowStep notificacion;
	private final ComprobanteReprocessService reprocessService;
	private final ComprobanteAuditoriaService comprobanteAuditoriaService;
	private final ComprobanteService comprobanteService;

	public ContextoWorkflow crearContexto(ComprobanteGeneracionRequest request) {
		return ContextoWorkflow.nuevo(request);
	}

	public ContextoWorkflow verificarIdempotencia(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null)
			return contexto;
		comprobanteService.obtenerPorEmpresaEIdTransaccion(contexto.getSolicitud().getIdEmpresa(),
				contexto.getSolicitud().getIdTransaccion()).ifPresent(c -> {
					contexto.asignarComprobante(c);
					contexto.setClaveAcceso(c.getClaveAcceso());
					contexto.setSecuencial(c.getSecuencial());
					contexto.marcarIdempotente();
				});
		return contexto;
	}

	public ContextoWorkflow validar(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, validacion);
		return contexto;
	}

	public ContextoWorkflow asignarSecuencial(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, asignacionSecuencial);
		return contexto;
	}

	@Transactional
	public ContextoWorkflow reservarComprobante(ContextoWorkflow contexto) {
		if (contexto == null || contexto.isIdempotente()) {
			return contexto;
		}

		asignarSecuencial(contexto);

		if (contexto.isIdempotente()) {
			return contexto;
		}

		generarClaveAcceso(contexto);
		persistirComprobante(contexto);
		return contexto;
	}

	public ContextoWorkflow generarClaveAcceso(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, generacionClaveAcceso);
		return contexto;
	}

	public ContextoWorkflow persistirComprobante(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, persistenciaComprobante);
		return contexto;
	}

	public ContextoWorkflow generarXml(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, generacionXml);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow validarXsd(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, validacionXsd);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow firmar(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, firmaElectronica);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow enviarSri(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, envioSri);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow autorizarSri(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, autorizacionSri);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow generarRide(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, generacionRide);
		persistirCambios(contexto);
		return contexto;
	}

	public ContextoWorkflow publicarNotificacion(ContextoWorkflow contexto) {
		ejecutarEtapa(contexto, notificacion);
		persistirCambios(contexto);
		return contexto;
	}

	private ResultadoEtapa ejecutarEtapa(ContextoWorkflow contexto, WorkflowStep step) {
		LocalDateTime fechaInicio = LocalDateTime.now();
		String estadoAnterior = contexto != null && contexto.getComprobante() != null
				? contexto.getComprobante().getEstadoProceso()
				: null;

		try {
			ResultadoEtapa resultado = step.ejecutar(contexto);
			registrarResultado(contexto, resultado);
			registrarAuditoria(contexto, step.etapa().name(), estadoAnterior, resultado, fechaInicio);
			return resultado;
		} catch (RuntimeException exception) {
			registrarAuditoriaError(contexto, step.etapa().name(), estadoAnterior, exception, fechaInicio);
			throw exception;
		}
	}

	private void registrarAuditoria(ContextoWorkflow contexto, String etapa, String estadoAnterior,
			ResultadoEtapa resultado, LocalDateTime fechaInicio) {
		Long comprobanteId = contexto == null ? null : contexto.getComprobanteId();
		if (comprobanteId != null) {
			comprobanteAuditoriaService.registrarResultado(comprobanteId, etapa, estadoAnterior, resultado, fechaInicio,
					LocalDateTime.now());
		}
	}

	private void registrarAuditoriaError(ContextoWorkflow contexto, String etapa, String estadoAnterior,
			RuntimeException exception, LocalDateTime fechaInicio) {
		Long comprobanteId = contexto == null ? null : contexto.getComprobanteId();
		if (comprobanteId != null) {
			String codigoError = exception instanceof WorkflowException workflowException
					? workflowException.getCodigo()
					: null;
			comprobanteAuditoriaService.registrarError(comprobanteId, etapa, estadoAnterior, codigoError,
					exception.getMessage(), fechaInicio, LocalDateTime.now());
		}
	}

	private void registrarResultado(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		contexto.registrarResultado(resultado);

		if (contexto.getComprobante() != null) {
			if (!resultado.isExitosa()) {
				contexto.getComprobante().setCodigoError(resultado.getCodigoError());
				contexto.getComprobante().setMensajeError(resultado.getMensaje());
			} else if (!EstadoProceso.ERROR.name().equals(resultado.getEstado())) {
				contexto.getComprobante().setCodigoError(null);
				contexto.getComprobante().setMensajeError(null);
			}
		}
	}

	private void persistirCambios(ContextoWorkflow contexto) {
		if (contexto.getComprobante() != null) {
			comprobanteService.guardar(contexto.getComprobante());
		}
	}

	public ComprobanteGeneracionResponse respuesta(ContextoWorkflow contexto) {
		var comprobante = contexto.getComprobante();
		var request = contexto.getSolicitud();

		String numeroComprobante = comprobante == null ? null
				: comprobante.getCodigoDocumento() + "-" + comprobante.getEstablecimiento().getCodigo() + "-"
						+ comprobante.getPuntoEmision().getCodigo() + "-" + comprobante.getSecuencial();

		boolean exitoso = comprobante != null
				&& (EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())
						|| EstadoProceso.AUTORIZADO.name().equals(comprobante.getEstadoProceso()));

		return ComprobanteGeneracionResponse.builder()
				.idTransaccion(request == null ? null : request.getIdTransaccion())
				.fechaInicio(contexto.getFechaInicio().atOffset(ZoneOffset.UTC))
				.fechaFin(OffsetDateTime.now(ZoneOffset.UTC)).exitoso(exitoso)
				.resultado(ResultadoResponse.builder().build()).claveAcceso(contexto.getClaveAcceso())
				.numeroComprobante(numeroComprobante).tipoDocumento(request == null ? null : comprobante.getCodigoDocumento())
				.estadoSri(contexto.getEstadoSri()).archivoPdf(contexto.getRide()).build();
	}

	public void reprocesarAutorizacion(Long comprobanteId) {
		reprocessService.reprocesarAutorizacion(comprobanteId);
	}
}
