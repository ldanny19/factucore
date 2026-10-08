package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

/**
 * Persiste el resultado producido por cada nodo del workflow.
 *
 * <p>Los Beans de negocio devuelven únicamente una salida textual y registran
 * su {@link ResultadoEtapa} en {@link ContextoWorkflow}. Esta clase se encarga
 * de aplicar ese resultado al comprobante y registrar la auditoría. No decide
 * la siguiente etapa; esa responsabilidad pertenece al XML de Camel.</p>
 */
@Component
@RequiredArgsConstructor
public class WorkflowStepResultHandler {

	private static final Logger log = LoggerFactory.getLogger(WorkflowStepResultHandler.class);

	private final ComprobanteAuditoriaService comprobanteAuditoriaService;
	private final ComprobanteService comprobanteService;
	private final MessageResolver messageResolver;

	public ContextoWorkflow registrar(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		if (contexto == null || resultado == null) {
			return contexto;
		}
		contexto.registrarResultado(resultado);
		logResultado(resultado);
		return persistir(contexto, resultado);
	}

	public ContextoWorkflow registrarResultado(Exchange exchange) {
		ContextoWorkflow contexto = exchange.getProperty("workflow.contexto", ContextoWorkflow.class);
		if (contexto == null) {
			contexto = exchange.getMessage().getBody(ContextoWorkflow.class);
		}
		if (contexto == null || contexto.getUltimoResultado() == null) {
			return contexto;
		}
		persistir(contexto, contexto.getUltimoResultado());
		exchange.getMessage().setBody(contexto);
		return contexto;
	}

	public ContextoWorkflow registrarExcepcion(Exchange exchange) {
		ContextoWorkflow contexto = exchange.getProperty("workflow.contexto", ContextoWorkflow.class);
		EtapaWorkflow etapa = resolverEtapa(exchange, contexto);
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		return registrarExcepcion(contexto, etapa, exception);
	}

	private EtapaWorkflow resolverEtapa(Exchange exchange, ContextoWorkflow contexto) {
		String etapaNombre = exchange.getProperty("workflow.etapa", String.class);
		if (etapaNombre != null && !etapaNombre.isBlank()) {
			try {
				return EtapaWorkflow.valueOf(etapaNombre);
			} catch (IllegalArgumentException exception) {
				log.warn("Etapa de workflow no reconocida: {}", etapaNombre);
			}
		}

		if (contexto != null && contexto.getEtapaActual() != null) {
			return contexto.getEtapaActual();
		}

		String routeId = exchange.getFromRouteId();
		if (routeId != null && routeId.startsWith("facturador-workflow-")) {
			String nombre = routeId.substring("facturador-workflow-".length()).toUpperCase();
			nombre = nombre.replace('-', '_');
			try {
				return EtapaWorkflow.valueOf(nombre);
			} catch (IllegalArgumentException exception) {
				log.warn("No fue posible determinar la etapa desde la ruta: {}", routeId);
			}
		}

		return null;
	}

	public ContextoWorkflow registrarExcepcion(ContextoWorkflow contexto, EtapaWorkflow etapa, Exception exception) {
		if (contexto == null) {
			return null;
		}

		String codigo = exception instanceof ApplicationException applicationException
				? applicationException.getCodigo()
				: MessageCodes.WORKFLOW_ETAPA_ERROR;

		Object[] parametros = exception instanceof ApplicationException applicationException
				? applicationException.getParametros()
				: new Object[] { etapa == null ? "DESCONOCIDA" : etapa.name() };

		if (etapa == null) {
			log.error("No fue posible determinar la etapa del workflow para registrar la excepción. codigo={}", codigo);
			return contexto;
		}

		String mensaje = messageResolver.resolver(codigo, parametros);
		ResultadoEtapa resultado = ResultadoEtapa.fallida(etapa, EstadoProceso.ERROR.name(), codigo, mensaje);
		return registrar(contexto, resultado);
	}

	private void logResultado(ResultadoEtapa resultado) {
		if (resultado.isExitosa()) {
			log.info("Workflow etapa={} resultado=OK estado={} mensaje={}", resultado.getEtapa(), resultado.getEstado(), resultado.getMensaje());
		} else {
			log.error("Workflow etapa={} resultado=ERROR codigo={} estado={} mensaje={}", resultado.getEtapa(), resultado.getCodigoError(), resultado.getEstado(), resultado.getMensaje());
		}
	}

	private ContextoWorkflow persistir(ContextoWorkflow contexto, ResultadoEtapa resultado) {
		String estadoAnterior = contexto.getComprobante() == null ? null : contexto.getComprobante().getEstadoProceso();
		LocalDateTime fechaInicio = LocalDateTime.now();

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

		if (contexto.getComprobanteId() != null && resultado.getEtapa() != null) {
			comprobanteAuditoriaService.registrarResultado(contexto.getComprobanteId(), resultado.getEtapa().name(),
					estadoAnterior, resultado, fechaInicio, LocalDateTime.now());
		}
		return contexto;
	}
}
