package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.ApplicationException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionXsdWorkflowStep  {
	private final XmlValidatorPort xmlValidator;
	private final ComprobanteEvidenciaPort evidenciaPort;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION_XSD;
	}

	/**
	 * Ejecuta la etapa y devuelve exclusivamente su resultado para Camel.
	 * El Bean no conoce ni decide el siguiente nodo del workflow.
	 */
	public String ejecutar(ContextoWorkflow contexto) {
		ResultadoEtapa resultado = ejecutarResultado(contexto);
		return contexto.registrarYObtenerSalida(resultado);
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getXml() == null || contexto.getXml().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}
		var definition = contexto.getDefinicionDocumento();
		if (definition == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA);
		}
		xmlValidator.validar(contexto.getXml(), definition);
		evidenciaPort.guardarXmlGenerado(contexto.getComprobanteId(), contexto.getXml(),
				contexto.getSolicitud().getUsuario());
		return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION_XSD, "VALIDADO");
	}
}
