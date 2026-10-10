package ec.dalara.factucore.application.workflow;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowEtapaExecutor {

	private static final Logger log = LoggerFactory.getLogger(WorkflowEtapaExecutor.class);

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;

	public String ejecutar(ContextoWorkflow contexto, EtapaWorkflow etapa, Supplier<ResultadoEtapa> ejecucion) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;

		log.debug(messageResolver.resolver(MessageCodes.LOG_WORKFLOW_ETAPA_INICIO, idTransaccion, etapa));
		if (contexto != null) {
			contexto.iniciarEtapa(etapa);
		}

		try {
			ResultadoEtapa resultado = ejecucion.get();
			String salida = workflowResultadoService.registrar(contexto, resultado);
			log.debug(messageResolver.resolver(MessageCodes.LOG_WORKFLOW_ETAPA_FIN, idTransaccion, etapa, salida));
			return salida;
		} catch (RuntimeException exception) {
			String codigo = exception instanceof ApplicationException applicationException
					? applicationException.getCodigo()
					: MessageCodes.WORKFLOW_ETAPA_ERROR;
			Object[] parametros = exception instanceof ApplicationException applicationException
					? applicationException.getParametros()
					: new Object[] { etapa.name() };
			String mensaje = messageResolver.resolver(codigo, parametros);
			ResultadoEtapa resultadoError = ResultadoEtapa.fallida(etapa, null, codigo, mensaje);
			if (contexto != null) {
				workflowResultadoService.registrar(contexto, resultadoError);
			}
			log.error(messageResolver.resolver(MessageCodes.LOG_WORKFLOW_ETAPA_ERROR, idTransaccion, etapa, codigo, mensaje),
					exception);
			String salida = resultadoError.salida();
			log.debug(messageResolver.resolver(MessageCodes.LOG_WORKFLOW_ETAPA_FIN, idTransaccion, etapa, salida));
			return salida;
		}
	}
}
