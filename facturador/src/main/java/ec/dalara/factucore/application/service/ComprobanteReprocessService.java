package ec.dalara.factucore.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.workflow.AutorizacionSriWorkflowStep;
import ec.dalara.factucore.application.workflow.GeneracionRideWorkflowStep;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReprocessService {

    private final ComprobanteRepository comprobanteRepository;
    private final AutorizacionSriWorkflowStep autorizacionSri;
    private final GeneracionRideWorkflowStep generacionRide;

    @Transactional
    public void reprocesarAutorizacion(Long comprobanteId) {
        var comprobante = comprobanteRepository.findByIdAndEstadoRegistro(
                comprobanteId, EstadoRegistro.ACTIVO).orElse(null);

        if (comprobante == null
                || !EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(comprobante.getEstadoProceso())) {
            return;
        }

        var contexto = ContextoWorkflow.existente(comprobante, null);
        var resultadoAutorizacion = autorizacionSri.ejecutar(contexto);
        contexto.registrarResultado(resultadoAutorizacion);

        if (EstadoProceso.AUTORIZADO.name().equals(resultadoAutorizacion.getEstado())) {
            var resultadoRide = generacionRide.ejecutar(contexto);
            contexto.registrarResultado(resultadoRide);
        }

        comprobanteRepository.save(comprobante);
    }
}
