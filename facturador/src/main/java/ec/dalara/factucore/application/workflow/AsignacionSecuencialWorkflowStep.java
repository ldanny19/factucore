package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.SecuencialPort;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AsignacionSecuencialWorkflowStep implements WorkflowStep {

	private final SecuencialPort secuencialPort;

	@Override
	public EtapaWorkflow etapa() {
		return EtapaWorkflow.ASIGNACION_SECUENCIAL;
	}

	@Override
	public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
		String secuencial = secuencialPort.obtenerSiguiente(contexto.getSolicitud().getIdEstablecimiento(),
				contexto.getSolicitud().getTipoDocumento());

		contexto.setSecuencial(secuencial);

		return ResultadoEtapa.exitosa(EtapaWorkflow.ASIGNACION_SECUENCIAL, "COMPLETADA");
	}
}