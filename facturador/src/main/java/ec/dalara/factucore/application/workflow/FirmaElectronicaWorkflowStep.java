package ec.dalara.factucore.application.workflow;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.port.out.CertificadoFirmaPasswordPort;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.service.FirmaElectronicaService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirmaElectronicaWorkflowStep   {

	private final ComprobanteEvidenciaPort evidenciaPort;
	private final FirmaElectronicaService firmaElectronicaService;
	private final CertificadoFirmaPasswordPort passwordPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.FIRMA_ELECTRONICA;
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
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}
		if (contexto.getXml() == null || contexto.getXml().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		char[] password = passwordPort.obtenerPassword(contexto.getSolicitud().getIdEmpresa());
		try {
			String xmlFirmado = firmaElectronicaService.firmar(contexto.getSolicitud().getIdEmpresa(),
					contexto.getXml(), password, contexto.getSolicitud().getFechaInicio());

			contexto.setXmlFirmado(xmlFirmado);
			String ruta = evidenciaPort.guardarXmlFirmado(contexto.getComprobanteId(), xmlFirmado,
					contexto.getSolicitud().getUsuario());
			contexto.getComprobante().setRutaXmlFirmado(ruta);

			return ResultadoEtapa.exitosa(etapa(), "COMPLETADA");
		} finally {
			Arrays.fill(password, '\0');
		}
	}
}
