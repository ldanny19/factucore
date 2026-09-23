package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.configuration.workflow.WorkflowReprocessProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowReprocessScheduler {

    private final ComprobanteRepository comprobanteRepository;
    private final WorkflowExecutionPort workflowExecutionPort;
    private final WorkflowReprocessProperties properties;
    private final WorkflowReprocessClaimService claimService;

    @Scheduled(fixedDelayString = "${factucore.workflow.reproceso.intervalo-ms:5000}")
    public void procesarPendientes() {
        if (!properties.isHabilitado()) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime bloqueadoHasta = ahora.plusNanos(Math.max(properties.getBloqueoMs(), 1000) * 1_000_000);

        comprobanteRepository.findTop100ByEstadoProcesoAndFechaProximoReprocesoLessThanEqualOrderByFechaProximoReprocesoAsc(
                EstadoProceso.AUTORIZACION_PENDIENTE.name(), ahora)
                .forEach(comprobante -> procesarSiReclamado(comprobante, ahora, bloqueadoHasta));
    }

    protected void procesarSiReclamado(Comprobante comprobante, LocalDateTime ahora, LocalDateTime bloqueadoHasta) {
        if (claimService.reclamar(comprobante.getId(), ahora, bloqueadoHasta)) {
            workflowExecutionPort.reprocesar(comprobante.getId());
        }
    }
}