package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionRideWorkflowStep implements WorkflowStep {

    private final RidePort ridePort;
    private final ComprobanteEvidenciaPort evidenciaPort;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.GENERACION_RIDE;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getComprobante() == null) {
            throw new WorkflowException(MessageCodes.RIDE_COMPROBANTE_REQUERIDO);
        }

        var comprobante = contexto.getComprobante();
        byte[] pdf = ridePort.generar(comprobante);
        contexto.setRide(pdf);

        String ruta = evidenciaPort.guardarRide(comprobante.getId(), pdf);
        comprobante.setRutaRide(ruta);
        comprobante.setEstadoProceso(EstadoProceso.RIDE_GENERADO.name());
        comprobante.setFechaProximoReproceso(null);

        return ResultadoEtapa.exitosa(etapa(), "RIDE_GENERADO",
                Map.of("archivoPdfGenerado", true, "rutaRide", ruta));
    }
}
