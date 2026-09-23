package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowReprocessClaimService {

    private final ComprobanteRepository comprobanteRepository;

    @Transactional
    public boolean reclamar(Long comprobanteId, LocalDateTime ahora, LocalDateTime bloqueadoHasta) {
        return comprobanteRepository.reclamarReproceso(
                comprobanteId, EstadoProceso.AUTORIZACION_PENDIENTE.name(), ahora, bloqueadoHasta) == 1;
    }
}