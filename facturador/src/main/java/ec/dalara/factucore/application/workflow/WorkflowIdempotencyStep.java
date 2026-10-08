package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowIdempotencyStep {

	private final ComprobanteService comprobanteService;

	public ContextoWorkflow ejecutar(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			return contexto;
		}

		comprobanteService.obtenerPorEmpresaEIdTransaccion(contexto.getSolicitud().getIdEmpresa(),
				contexto.getSolicitud().getIdTransaccion()).ifPresent(comprobante -> {
					contexto.asignarComprobante(comprobante);
					contexto.setClaveAcceso(comprobante.getClaveAcceso());
					contexto.setSecuencial(comprobante.getSecuencial());
					contexto.marcarIdempotente();
					contexto.registrarResultado(ResultadoEtapa.exitosa(EtapaWorkflow.RECEPCION, "IDEMPOTENTE"));
				});

		return contexto;
	}
}
