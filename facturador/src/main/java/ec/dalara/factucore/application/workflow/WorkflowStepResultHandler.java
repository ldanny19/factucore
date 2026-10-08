package ec.dalara.factucore.application.workflow;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowStepResultHandler {

	private final ComprobanteAuditoriaService comprobanteAuditoriaService;
	private final ComprobanteService comprobanteService;
	private final MessageResolver messageResolver;

	public ContextoWorkflow registrar(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		if (contexto == null || resultado == null) {
			return contexto;
		}

		contexto.registrarResultado(resultado);

		if (contexto.getComprobante() != null) {
			if (!resultado.isExitosa()) {
				contexto.getComprobante().setCodigoError(resultado.getCodigoError());
				contexto.getComprobante().setMensajeError(resultado.getMensaje());
				if (resultado.getEstado() != null) {
					contexto.getComprobante().setEstadoProceso(resultado.getEstado());
				}
			} else if (!EstadoProceso.ERROR.name().equals(resultado.getEstado())) {
				contexto.getComprobante().setCodigoError(null);
				contexto.getComprobante().setMensajeError(null);
			}

			comprobanteService.guardar(contexto.getComprobante());
		}

		return contexto;
	}

	public ContextoWorkflow registrarResultado(Exchange exchange) {
		ContextoWorkflow contexto = exchange.getProperty("workflow.contexto", ContextoWorkflow.class);
		ResultadoEtapa resultado = exchange.getMessage().getBody(ResultadoEtapa.class);
		return registrar(contexto, resultado);
	}

	public ContextoWorkflow registrarExcepcion(Exchange exchange) {
		ContextoWorkflow contexto = exchange.getProperty("workflow.contexto", ContextoWorkflow.class);
		String etapaNombre = exchange.getProperty("workflow.etapa", String.class);
		EtapaWorkflow etapa = etapaNombre == null ? null : EtapaWorkflow.valueOf(etapaNombre);
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		return registrarExcepcion(contexto, etapa, exception);
	}

	public ContextoWorkflow registrarExcepcion(ContextoWorkflow contexto, EtapaWorkflow etapa, Exception exception) {
		if (contexto == null) {
			return null;
		}

		String codigo = exception instanceof WorkflowException workflowException
				? workflowException.getCodigo()
				: MessageCodes.WORKFLOW_ETAPA_ERROR;

		Object[] parametros = exception instanceof WorkflowException workflowException
				? workflowException.getParametros()
				: new Object[] { etapa == null ? "DESCONOCIDA" : etapa.name() };

		String mensaje = messageResolver.resolver(codigo, parametros);

		ResultadoEtapa resultado = ResultadoEtapa.fallida(etapa, EstadoProceso.ERROR.name(), codigo, mensaje);
		return registrar(contexto, resultado);
	}
}
