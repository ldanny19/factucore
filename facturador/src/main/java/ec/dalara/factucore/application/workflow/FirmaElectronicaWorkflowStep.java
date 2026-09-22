package ec.dalara.factucore.application.workflow;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.CertificadoFirmaPasswordPort;
import ec.dalara.factucore.application.service.FirmaElectronicaService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirmaElectronicaWorkflowStep implements WorkflowStep {

    private final FirmaElectronicaService firmaElectronicaService;
    private final CertificadoFirmaPasswordPort passwordPort;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.FIRMA_ELECTRONICA;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getSolicitud() == null) {
            throw new WorkflowException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
        }

        if (contexto.getXml() == null || contexto.getXml().isBlank()) {
            throw new WorkflowException(MessageCodes.FIRMA_XML_REQUERIDO);
        }

        if (contexto.getXmlFirmado() != null && !contexto.getXmlFirmado().isBlank()) {
            return ResultadoEtapa.exitosa(
                    EtapaWorkflow.FIRMA_ELECTRONICA,
                    "YA_FIRMADA");
        }

        char[] password = passwordPort.obtenerPassword(contexto.getSolicitud().getIdEmpresa());
        try {
            String xmlFirmado = firmaElectronicaService.firmar(
                    contexto.getSolicitud().getIdEmpresa(),
                    contexto.getXml(),
                    password,
                    contexto.getSolicitud().getFechaInicio().toLocalDateTime());

            contexto.setXmlFirmado(xmlFirmado);

            return ResultadoEtapa.exitosa(
                    EtapaWorkflow.FIRMA_ELECTRONICA,
                    "COMPLETADA");
        } finally {
            Arrays.fill(password, '\\0');
        }
    }
}
