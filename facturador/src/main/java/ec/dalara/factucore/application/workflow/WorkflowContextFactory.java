package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

/**
 * Crea el contexto mutable que acompaña una ejecución completa de facturación.
 *
 * <p>El contexto es el estado de trabajo que comparten los nodos del workflow:
 * solicitud, comprobante, secuencial, clave de acceso, XML, firma, SRI, RIDE y
 * resultados. No contiene la definición de las transiciones.</p>
 */
@Component
public class WorkflowContextFactory {

	public ContextoWorkflow crear(ComprobanteGeneracionRequest request) {
		return ContextoWorkflow.nuevo(request);
	}
}
