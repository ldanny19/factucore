package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;

import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;

import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
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

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;
	private final ComprobanteWorkflowResponseFactory responseFactory;

	private boolean resultadoFinalExitoso(ContextoWorkflow contexto) {
		for (EtapaWorkflow etapa : EtapaWorkflow.values()) {
			if (etapa == EtapaWorkflow.INICIO || etapa == EtapaWorkflow.GENERAR_RESPUESTA || etapa == EtapaWorkflow.RESPUESTA_ERROR_NOTIFICACION || etapa == EtapaWorkflow.FIN) continue;
			ResultadoEtapa resultado = contexto.getResultado(etapa);
			if (resultado != null && !resultado.isExitosa()) return false;
		}
		return true;
	}

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERAR_RESPUESTA;
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
			boolean exitoso = contexto != null
					&& resultadoFinalExitoso(contexto);

			if (contexto != null) {
				contexto.setExitosoFinal(exitoso);
				contexto.setRespuesta(responseFactory.crear(contexto));
			}
			ResultadoEtapa resultado = ResultadoEtapa.exitosa(etapa(), "COMPLETADA");
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
	}}
