package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import lombok.RequiredArgsConstructor;

/**
 * Prepara la respuesta pública para un cierre normal del workflow.
 *
 * <p>Se ejecuta tanto después de un proceso exitoso como después de un error
 * crítico. El nodo no termina el workflow ni decide la transición; devuelve
 * {@code OK} y el XML de Camel conduce posteriormente al nodo {@code FIN}.</p>
 */
@Component
@RequiredArgsConstructor
public class GenerarRespuestaWorkflowStep {

	private final ComprobanteWorkflowResponseFactory responseFactory;

	public String ejecutar(ContextoWorkflow contexto) {
		boolean exitoso = contexto != null
				&& contexto.getUltimoResultado() != null
				&& contexto.getUltimoResultado().isExitosa();

		if (contexto != null) {
			contexto.setExitosoFinal(exitoso);
			contexto.setRespuesta(responseFactory.crear(contexto));
		}
		return "OK";
	}
}
