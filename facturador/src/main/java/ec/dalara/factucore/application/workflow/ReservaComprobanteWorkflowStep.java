package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaComprobanteWorkflowStep {

	private final AsignacionSecuencialWorkflowStep asignacionSecuencial;
	private final GeneracionClaveAccesoWorkflowStep generacionClaveAcceso;
	private final PersistenciaComprobanteWorkflowStep persistenciaComprobante;
	private final WorkflowStepResultHandler resultHandler;

	@Transactional
	public ContextoWorkflow ejecutar(ContextoWorkflow contexto) {
		if (contexto == null || contexto.isIdempotente()) {
			return contexto;
		}

		contexto = ejecutarEtapa(contexto, asignacionSecuencial.ejecutar(contexto));
		if (!contexto.getUltimoResultado().isExitosa() || contexto.isIdempotente()) {
			return contexto;
		}

		contexto = ejecutarEtapa(contexto, generacionClaveAcceso.ejecutar(contexto));
		if (!contexto.getUltimoResultado().isExitosa()) {
			return contexto;
		}

		return ejecutarEtapa(contexto, persistenciaComprobante.ejecutar(contexto));
	}

	private ContextoWorkflow ejecutarEtapa(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		return resultHandler.registrar(contexto, resultado);
	}
}
