package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;


import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;


import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionWorkflowStep   {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final NotificacionPort notificacionPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.NOTIFICACION;
	}

	/** Ejecuta la notificación y devuelve a Camel solo OK o ERROR. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getComprobante() == null) {
			throw new ApplicationException(MessageCodes.NOTIFICACION_COMPROBANTE_REQUERIDO);
		}

		var comprobante = contexto.getComprobante();

		if (!EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())) {
			return ResultadoEtapa.exitosa(etapa(), "NOTIFICACION_ERROR");
		}

		try {
			notificacionPort.publicar(contexto);
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.RIDE_GENERADO.name(),
					Map.of("notificacionPublicada", true));
		} catch (RuntimeException exception) {
			return ResultadoEtapa.exitosa(etapa(), "NOTIFICACION_ERROR");
		}
	}
}
