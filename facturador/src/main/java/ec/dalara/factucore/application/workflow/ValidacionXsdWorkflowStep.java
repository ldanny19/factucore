package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionXsdWorkflowStep implements WorkflowStep {
    private final XmlValidatorPort xmlValidator;

    @Override public EtapaWorkflow etapa() { return EtapaWorkflow.VALIDACION_XSD; }

    @Override public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getXml() == null || contexto.getXml().isBlank()) {
            throw new WorkflowException(MessageCodes.FIRMA_XML_REQUERIDO);
        }
        var definition = contexto.getDefinicionDocumento();
        if (definition == null) {
            throw new WorkflowException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA);
        }
        xmlValidator.validar(contexto.getXml(), definition);
        return ResultadoEtapa.exitosa(EtapaWorkflow.VALIDACION_XSD, "VALIDADO");
    }
}
