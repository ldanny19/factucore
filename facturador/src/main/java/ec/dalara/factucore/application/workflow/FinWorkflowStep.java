package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

/**
 * Nodo terminal único del workflow de facturación.
 *
 * <p>FIN no contiene reglas de negocio ni decide el resultado. Su única
 * responsabilidad es marcar el cierre lógico del grafo. La ruta Camel entrega
 * inmediatamente después la respuesta que fue preparada por la etapa de
 * respuesta correspondiente.</p>
 */
@Component
public class FinWorkflowStep {

	public String ejecutar(ContextoWorkflow contexto) {
		return "FIN";
	}
}
