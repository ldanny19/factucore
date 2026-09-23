package ec.dalara.factucore.application.workflow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.application.service.EstablecimientoService;
import ec.dalara.factucore.application.service.PuntoEmisionService;
import ec.dalara.factucore.application.service.SecuencialService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AsignacionSecuencialWorkflowStep implements WorkflowStep {

    private final EstablecimientoService establecimientoService;
    private final PuntoEmisionService puntoEmisionService;
    private final SecuencialService secuencialService;
    private final ComprobanteService comprobanteService;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.ASIGNACION_SECUENCIAL;
    }

    @Override
    @Transactional
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        var solicitud = contexto.getSolicitud();

        Establecimiento establecimiento = establecimientoService
                .obtenerPorEmpresaYCodigo(solicitud.getIdEmpresa(), solicitud.getCodigoEstablecimiento())
                .orElseThrow(() -> new WorkflowException(MessageCodes.SECUENCIAL_NO_ENCONTRADO));

        PuntoEmision puntoEmision = puntoEmisionService
                .obtenerPorEstablecimientoYCodigo(establecimiento.getId(), solicitud.getPuntoEmision())
                .orElseThrow(() -> new WorkflowException(MessageCodes.SECUENCIAL_NO_ENCONTRADO));

        var secuencial = secuencialService.bloquearSecuencial(
                puntoEmision.getId(), solicitud.getTipoDocumento());

        var existente = comprobanteService.obtenerPorEmpresaEIdTransaccion(
                solicitud.getIdEmpresa(), solicitud.getIdTransaccion());

        if (existente.isPresent()) {
            contexto.asignarComprobante(existente.get());
            contexto.setClaveAcceso(existente.get().getClaveAcceso());
            contexto.setSecuencial(existente.get().getSecuencial());
            contexto.marcarIdempotente();

            return ResultadoEtapa.exitosa(
                    EtapaWorkflow.ASIGNACION_SECUENCIAL,
                    "IDEMPOTENTE");
        }

        Long siguiente = secuencialService.consumirSiguienteSecuencial(secuencial);
        contexto.setSecuencial(String.format("%09d", siguiente));

        return ResultadoEtapa.exitosa(EtapaWorkflow.ASIGNACION_SECUENCIAL, "COMPLETADA");
    }
}
