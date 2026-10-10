package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import java.util.Arrays;

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

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final FirmaElectronicaService firmaElectronicaService;
	private final CertificadoFirmaPasswordPort passwordPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.FIRMA_ELECTRONICA;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}
		if (contexto.getXml() == null || contexto.getXml().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		char[] password = passwordPort.obtenerPassword();
		try {
			String xmlFirmado = firmaElectronicaService.firmar(contexto.getSolicitud().getIdEmpresa(),
					contexto.getXml(), password, contexto.getSolicitud().getFechaInicio());

			contexto.setXmlFirmado(xmlFirmado);
			String ruta = evidenciaPort.guardarXmlFirmado(contexto.getComprobanteId(), xmlFirmado,
					contexto.getSolicitud().getUsuario());
			contexto.getComprobante().setRutaXmlFirmado(ruta);
			contexto.getComprobante().setEstadoProceso(EstadoProceso.FIRMADO.name());

			return ResultadoEtapa.exitosa(etapa(), EstadoProceso.FIRMADO.name());
		} finally {
			Arrays.fill(password, '\0');
		}
	}
}
