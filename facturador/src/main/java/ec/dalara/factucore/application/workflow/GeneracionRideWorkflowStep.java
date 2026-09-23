package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionRideWorkflowStep implements WorkflowStep {

    private final RidePort ridePort;

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
        if (comprobante.getArchivoPdf() != null && comprobante.getArchivoPdf().length > 0) {
            contexto.setRide(comprobante.getArchivoPdf());
            return ResultadoEtapa.exitosa(etapa(), "RIDE_GENERADO",
                    Map.of("archivoPdfGenerado", true, "reutilizado", true));
        }

        byte[] pdf = ridePort.generar(comprobante);
        contexto.setRide(pdf);
        comprobante.setArchivoPdf(pdf);
        comprobante.setEstadoProceso("RIDE_GENERADO");
        comprobante.setFechaProximoReproceso(null);

        return ResultadoEtapa.exitosa(etapa(), "RIDE_GENERADO",
                Map.of("archivoPdfGenerado", true));
    }
}
