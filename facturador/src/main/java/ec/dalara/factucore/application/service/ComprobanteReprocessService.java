package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.workflow.AutorizacionSriWorkflowStep;
import ec.dalara.factucore.application.workflow.GeneracionRideWorkflowStep;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReprocessService {

	private final ComprobanteRepository comprobanteRepository;
	private final AutorizacionSriWorkflowStep autorizacionSri;
	private final GeneracionRideWorkflowStep generacionRide;
	private final ComprobanteAuditoriaService comprobanteAuditoriaService;

	@Transactional
	public void reprocesarAutorizacion(Long comprobanteId) {
		var comprobante = comprobanteRepository.findByIdAndEstadoRegistro(comprobanteId, EstadoRegistro.ACTIVO)
				.orElse(null);

		if (comprobante == null
				|| !EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(comprobante.getEstadoProceso())) {
			return;
		}

		var contexto = ContextoWorkflow.existente(comprobante, null);
		var resultadoAutorizacion = ejecutarYAuditar(contexto, autorizacionSri);
		contexto.registrarResultado(resultadoAutorizacion);

		if (EstadoProceso.AUTORIZADO.name().equals(resultadoAutorizacion.getEstado())) {
			var resultadoRide = ejecutarYAuditar(contexto, generacionRide);
			contexto.registrarResultado(resultadoRide);
		}

		if (!resultadoAutorizacion.isExitosa()) {
			comprobante.setCodigoError(resultadoAutorizacion.getCodigoError());
			comprobante.setMensajeError(resultadoAutorizacion.getMensaje());
		} else if (EstadoProceso.AUTORIZADO.name().equals(resultadoAutorizacion.getEstado())) {
			comprobante.setCodigoError(null);
			comprobante.setMensajeError(null);
		}

		comprobanteRepository.save(comprobante);
	}

	private ResultadoEtapa ejecutarYAuditar(ContextoWorkflow contexto,
			ec.dalara.factucore.application.workflow.WorkflowStep step) {
		LocalDateTime fechaInicio = LocalDateTime.now();
		String estadoAnterior = contexto.getComprobante() == null ? null : contexto.getComprobante().getEstadoProceso();

		try {
			var resultado = step.ejecutar(contexto);
			comprobanteAuditoriaService.registrarResultado(contexto.getComprobanteId(), step.etapa().name(),
					estadoAnterior, resultado, fechaInicio, LocalDateTime.now());
			return resultado;
		} catch (RuntimeException exception) {
			comprobanteAuditoriaService.registrarError(contexto.getComprobanteId(), step.etapa().name(), estadoAnterior,
					exception instanceof ec.dalara.factucore.application.workflow.WorkflowException workflowException
							? workflowException.getCodigo()
							: null,
					exception.getMessage(), fechaInicio, LocalDateTime.now());
			throw exception;
		}
	}
}
