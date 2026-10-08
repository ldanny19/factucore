package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.validation.ComprobanteGeneracionValidator;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionComprobanteWorkflowStep  {
	private final ComprobanteGeneracionValidator validator;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION;
	}

	/** Ejecuta la lógica de negocio y expone a Camel solo la salida de la etapa. */
	public String ejecutar(ContextoWorkflow contexto) {
		ResultadoEtapa resultado = ejecutarResultado(contexto);
		return contexto.registrarYObtenerSalida(resultado);
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
		}
		var resultado = validator.validar(contexto.getSolicitud());
		if (!resultado.esValido()) {
			var error = resultado.getErrores().isEmpty() ? null : resultado.getErrores().get(0);
			return ResultadoEtapa.fallida(EtapaWorkflow.VALIDACION, "RECHAZADA",
					error == null ? MessageCodes.COMPROBANTE_REQUEST_REQUERIDO : error.getCodigo(),
					error == null ? null : error.getMensaje());
		}
		return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION, "VALIDADA");
	}
}
