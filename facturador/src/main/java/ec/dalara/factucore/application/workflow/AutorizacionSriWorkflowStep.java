package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.application.service.SriService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutorizacionSriWorkflowStep   {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final MessageResolver messageResolver;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final SriService sriService;
	private final SriProperties sriProperties;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.AUTORIZACION_SRI;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new ApplicationException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
		}

		if (EstadoProceso.ENVIADO_SRI.name().equals(contexto.getComprobante().getEstadoProceso())) {
			esperar(sriProperties.getAutorizacion().getEsperaInicialMs());
		}

		SriResponse respuesta = sriService.autorizar(contexto.getClaveAcceso());
		contexto.setEstadoSri(respuesta.estado());

		if ("AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
			contexto.setNumeroAutorizacion(respuesta.identificador());
			contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZADO.name());
			contexto.getComprobante().setFechaProximoReproceso(null);
			contexto.getComprobante().setNumeroAutorizacion(respuesta.identificador());
			evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta,
					contexto.getSolicitud().getUsuario());
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.AUTORIZADO.name(),
					Map.of("numeroAutorizacion", respuesta.identificador() == null ? "" : respuesta.identificador()));
		}

		if ("EN PROCESO".equalsIgnoreCase(respuesta.estado())) {
			long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaReintentoMs(), 1000);
			contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZACION_PENDIENTE.name());
			contexto.getComprobante()
					.setFechaProximoReproceso(java.time.LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.AUTORIZACION_PENDIENTE.name());
		}

		contexto.getComprobante().setEstadoProceso(EstadoProceso.ERROR.name());
		contexto.getComprobante().setFechaProximoReproceso(null);
		evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta,
				contexto.getSolicitud().getUsuario());
		var mensaje = respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0);
		return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
				mensaje == null || mensaje.identificador() == null ? MessageCodes.SRI_RESPUESTA_INVALIDA
						: mensaje.identificador(),
				mensaje == null ? null : mensaje.mensaje());
	}

	private void esperar(long esperaMs) {
		if (esperaMs <= 0)
			return;
		try {
			Thread.sleep(esperaMs);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ApplicationException(MessageCodes.SRI_ERROR_COMUNICACION);
		}
	}
}
