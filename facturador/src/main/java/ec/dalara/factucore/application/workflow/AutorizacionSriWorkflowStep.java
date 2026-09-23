package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.application.service.SriService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutorizacionSriWorkflowStep implements WorkflowStep {

    private final ComprobanteEvidenciaPort evidenciaPort;
    private final SriService sriService;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.AUTORIZACION_SRI;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getClaveAcceso() == null
                || contexto.getClaveAcceso().isBlank()) {
            throw new WorkflowException(MessageCodes.SRI_CLAVE_ACCESO_REQUERIDA);
        }

        SriResponse respuesta = sriService.autorizar(contexto.getClaveAcceso());
        String ruta = evidenciaPort.guardarRespuestaSriAutorizacion(contexto.getComprobanteId(), respuesta);
        contexto.getComprobante().setRutaRespuestaSri(ruta);
        contexto.setEstadoSri(respuesta.estado());

        if (respuesta.exitoso()) {
            contexto.setNumeroAutorizacion(respuesta.identificador());
            return ResultadoEtapa.exitosa(etapa(), respuesta.estado(),
                    Map.of("numeroAutorizacion",
                            respuesta.identificador() == null ? "" : respuesta.identificador()));
        }

        var mensaje = respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0);
        return ResultadoEtapa.fallida(etapa(), respuesta.estado(),
                mensaje == null || mensaje.identificador() == null
                        ? MessageCodes.SRI_RESPUESTA_INVALIDA : mensaje.identificador(),
                mensaje == null ? null : mensaje.mensaje());
    }
}
