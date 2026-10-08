package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.shared.MessageCodes;

import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

/**
 * Nodo terminal único del workflow de facturación.
 *
 * <p>FIN no contiene reglas de negocio ni decide transiciones. Su operación
 * principal devuelve {@code FIN}, que representa exclusivamente el cierre del
 * grafo. La respuesta pública ya fue preparada por el nodo de respuesta y este
 * nodo únicamente la entrega al final de la ruta Camel.</p>
 */
@Component
@RequiredArgsConstructor
public class FinWorkflowStep {

	private static final Logger log = LoggerFactory.getLogger(FinWorkflowStep.class);

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;
	private final ComprobanteWorkflowResponseFactory responseFactory;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.FIN;
	}

	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		if (contexto != null) {
			contexto.iniciarEtapa(etapa());
		}
		try {
			ResultadoEtapa resultado = ResultadoEtapa.exitosa(etapa(), "FIN");
			if (contexto != null) {
				contexto.registrarResultado(resultado);
			}
			String salida = resultado.salida();
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			String codigo = exception instanceof ApplicationException applicationException
					? applicationException.getCodigo()
					: MessageCodes.WORKFLOW_ETAPA_ERROR;
			Object[] parametros = exception instanceof ApplicationException applicationException
					? applicationException.getParametros()
					: new Object[] { etapa().name() };
			String mensaje = messageResolver.resolver(codigo, parametros);
			ResultadoEtapa resultadoError = ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), codigo, mensaje);
			if (contexto != null) {
				workflowResultadoService.registrar(contexto, resultadoError);
			}
			log.error("ID_TRANSACCION={} - Error Etapa {} - codigo={} - mensaje={}", idTransaccion, etapa(), codigo, mensaje,
					exception);
			String salida = resultadoError.salida();
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		}
	}
	public ComprobanteGeneracionResponse respuesta(ContextoWorkflow contexto) {
		if (contexto == null) {
			return null;
		}
		if (contexto.getRespuesta() == null) {
			contexto.setRespuesta(responseFactory.crear(contexto));
		}
		return contexto.getRespuesta();
	}
}
