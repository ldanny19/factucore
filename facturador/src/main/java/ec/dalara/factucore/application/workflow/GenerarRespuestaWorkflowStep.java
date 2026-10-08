package ec.dalara.factucore.application.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(GenerarRespuestaWorkflowStep.class);

	private final ComprobanteWorkflowResponseFactory responseFactory;

	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa GENERAR_RESPUESTA", idTransaccion);
		try {
			boolean exitoso = contexto != null
					&& contexto.getUltimoResultado() != null
					&& contexto.getUltimoResultado().isExitosa();

			if (contexto != null) {
				contexto.setExitosoFinal(exitoso);
				contexto.setRespuesta(responseFactory.crear(contexto));
			}
			log.debug("ID_TRANSACCION={} - Fin Etapa GENERAR_RESPUESTA - Resultado={}", idTransaccion, "OK");
			return "OK";
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa GENERAR_RESPUESTA - Resultado=ERROR", idTransaccion);
			throw exception;
		}
	}}
