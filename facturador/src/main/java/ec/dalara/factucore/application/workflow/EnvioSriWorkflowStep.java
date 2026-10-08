package ec.dalara.factucore.application.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

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
public class EnvioSriWorkflowStep   {

	private final SriService sriService;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.ENVIO_SRI;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		log.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		try {
			ResultadoEtapa resultado = ejecutarResultado(contexto);
			String salida = contexto.registrarYObtenerSalida(resultado);
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			log.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado=ERROR", idTransaccion, etapa());
			throw exception;
		}
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getXmlFirmado() == null || contexto.getXmlFirmado().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		SriResponse respuesta = sriService.enviar(contexto.getXmlFirmado());
		contexto.setEstadoSri(respuesta.estado());

		if (respuesta.exitoso()) {
			contexto.getComprobante().setEstadoProceso(EstadoProceso.ENVIADO_SRI.name());
			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.ENVIADO_SRI.name());
		}

		contexto.getComprobante().setEstadoProceso(EstadoProceso.ERROR.name());
		return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
				respuesta.mensajes().isEmpty() ? MessageCodes.SRI_RESPUESTA_INVALIDA
						: respuesta.mensajes().get(0).identificador(),
				respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0).mensaje());
	}
}
