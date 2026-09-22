package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;
import ec.dalara.factucore.application.validation.ComprobanteGeneracionValidator;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionComprobanteWorkflowStep implements WorkflowStep {
    private final ComprobanteGeneracionValidator validator;

    @Override public EtapaWorkflow etapa() { return EtapaWorkflow.VALIDACION; }

    @Override public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getSolicitud() == null) {
            throw new WorkflowException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
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
