package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.NotificacionPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacionWorkflowStep implements WorkflowStep {

    private final NotificacionPort notificacionPort;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.NOTIFICACION;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getComprobante() == null) {
            throw new WorkflowException(MessageCodes.NOTIFICACION_COMPROBANTE_REQUERIDO);
        }

        var comprobante = contexto.getComprobante();
        if (!EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())
                && !EstadoProceso.AUTORIZADO.name().equals(comprobante.getEstadoProceso())) {
            return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
                    MessageCodes.NOTIFICACION_PUBLICACION_ERROR,
                    null);
        }

        try {
            notificacionPort.publicar(contexto);
            return ResultadoEtapa.exitosa(etapa(), EstadoProceso.RIDE_GENERADO.name(),
                    Map.of("notificacionPublicada", true));
        } catch (RuntimeException exception) {
            return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
                    MessageCodes.NOTIFICACION_PUBLICACION_ERROR, null);
        }
    }
}
