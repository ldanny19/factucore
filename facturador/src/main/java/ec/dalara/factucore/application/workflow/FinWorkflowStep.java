package ec.dalara.factucore.application.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
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

	private final ComprobanteWorkflowResponseFactory responseFactory;

	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa FIN", idTransaccion);
		try {
			String resultado = "FIN";
			log.debug("ID_TRANSACCION={} - Fin Etapa FIN - Resultado={}", idTransaccion, resultado);
			return resultado;
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa FIN - Resultado=ERROR", idTransaccion);
			throw exception;
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
