package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowResultadoService {

	private final ComprobanteAuditoriaService comprobanteAuditoriaService;
	private final ComprobanteService comprobanteService;

	public String registrar(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		contexto.registrarResultado(resultado);
		persistir(contexto, resultado);
		return resultado.salida();
	}

	private void persistir(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		String estadoAnterior = contexto.getComprobante() == null ? null : contexto.getComprobante().getEstadoProceso();
		LocalDateTime fechaInicio = LocalDateTime.now();

		if (contexto.getComprobante() != null) {
			if (!resultado.isExitosa()) {
				contexto.getComprobante().setCodigoError(resultado.getCodigoError());
				contexto.getComprobante().setMensajeError(resultado.getMensaje());
			} else {
				contexto.getComprobante().setCodigoError(null);
				contexto.getComprobante().setMensajeError(null);
			}
			comprobanteService.guardar(contexto.getComprobante());
		}

		if (contexto.getComprobanteId() != null && resultado.getEtapa() != null) {
			comprobanteAuditoriaService.registrarResultado(contexto.getComprobanteId(), resultado.getEtapa().name(),
					estadoAnterior, resultado, fechaInicio, LocalDateTime.now());
		}
	}
}
