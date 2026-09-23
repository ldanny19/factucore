package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
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
public class EnvioSriWorkflowStep implements WorkflowStep {

    private final ComprobanteEvidenciaPort evidenciaPort;
    private final SriService sriService;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.ENVIO_SRI;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getXmlFirmado() == null
                || contexto.getXmlFirmado().isBlank()) {
            throw new WorkflowException(MessageCodes.FIRMA_XML_REQUERIDO);
        }

        SriResponse respuesta = sriService.enviar(contexto.getXmlFirmado());
        String ruta = evidenciaPort.guardarRespuestaSriRecepcion(contexto.getComprobanteId(), respuesta);
        contexto.getComprobante().setRutaRespuestaSri(ruta);
        contexto.setEstadoSri(respuesta.estado());

        if (respuesta.exitoso()) {
            contexto.getComprobante().setEstadoProceso(EstadoProceso.ENVIADO_SRI.name());
            return ResultadoEtapa.exitosa(etapa(), EstadoProceso.ENVIADO_SRI.name());
        }

        contexto.getComprobante().setEstadoProceso(EstadoProceso.ERROR.name());
        return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
                        respuesta.mensajes().isEmpty()
                                ? MessageCodes.SRI_RESPUESTA_INVALIDA
                                : respuesta.mensajes().get(0).identificador(),
                        respuesta.mensajes().isEmpty() ? null
                                : respuesta.mensajes().get(0).mensaje());
    }
}
