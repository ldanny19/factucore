package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.WorkflowExecutionPort;\nimport ec.dalara.factucore.infrastructure.configuration.workflow.WorkflowReprocessProperties;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowReprocessScheduler {

    private static final String ESTADO_AUTORIZACION_PENDIENTE = "AUTORIZACION_PENDIENTE";

    private final ComprobanteRepository comprobanteRepository;
    private final WorkflowExecutionPort workflowExecutionPort;
    private final WorkflowReprocessProperties properties;

    @Scheduled(fixedDelayString = "${factucore.workflow.reproceso.intervalo-ms:5000}")
    public void procesarPendientes() {
        if (!properties.isHabilitado()) {
            return;
        }

        comprobanteRepository
                .findTop100ByEstadoProcesoAndFechaProximoReprocesoLessThanEqualOrderByFechaProximoReprocesoAsc(
                        ESTADO_AUTORIZACION_PENDIENTE, LocalDateTime.now())
                .forEach(comprobante -> workflowExecutionPort.reprocesar(comprobante.getId()));
    }
}
