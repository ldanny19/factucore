package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;


import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.application.service.SriService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnvioSriWorkflowStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(EnvioSriWorkflowStep.class);

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final SriService sriService;
	private final MessageResolver messageResolver;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.ENVIO_SRI;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private void registrarRespuestaSri(ContextoWorkflow contexto, SriResponse respuesta) {
		if (!LOGGER.isDebugEnabled()) {
			return;
		}

		String idTransaccion = contexto.getSolicitud() == null ? null : contexto.getSolicitud().getIdTransaccion();
		LOGGER.debug(messageResolver.resolver(MessageCodes.LOG_SRI_RESPUESTA_COMPLETA, idTransaccion,
				etapa().name(), respuesta.respuestaXml()));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getXmlFirmado() == null || contexto.getXmlFirmado().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		SriResponse respuesta = sriService.enviar(contexto.getXmlFirmado());
		contexto.setEstadoSri(respuesta.estado());
		registrarRespuestaSri(contexto, respuesta);

		if (respuesta.exitoso()) {
			contexto.getComprobante().setEstadoProceso(EstadoProceso.ENVIADO_SRI.name());
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.ENVIADO_SRI.name());
		}

		contexto.getComprobante().setEstadoProceso(EstadoProceso.ERROR.name());
		return ResultadoEtapa.fallida(etapa(), "RECHAZADO",
				respuesta.mensajes().isEmpty() ? MessageCodes.SRI_RESPUESTA_INVALIDA
						: respuesta.mensajes().get(0).identificador(),
				respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0).mensaje());
	}
}
