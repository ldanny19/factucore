package ec.dalara.factucore.adapter.out.workflow;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.workflow.AsignacionSecuencialWorkflowStep;
import ec.dalara.factucore.application.workflow.FirmaElectronicaWorkflowStep;
import ec.dalara.factucore.application.workflow.EnvioSriWorkflowStep;
import ec.dalara.factucore.application.workflow.AutorizacionSriWorkflowStep;
import ec.dalara.factucore.application.workflow.GeneracionXmlWorkflowStep;
import ec.dalara.factucore.application.workflow.GeneracionClaveAccesoWorkflowStep;
import ec.dalara.factucore.application.workflow.ValidacionComprobanteWorkflowStep;
import ec.dalara.factucore.application.workflow.ValidacionXsdWorkflowStep;\nimport ec.dalara.factucore.application.service.ComprobanteReprocessService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CamelWorkflowStepExecutor {
    private final AsignacionSecuencialWorkflowStep asignacionSecuencial;
    private final ValidacionComprobanteWorkflowStep validacionComprobante;
    private final GeneracionClaveAccesoWorkflowStep generacionClaveAcceso;
    private final ValidacionXsdWorkflowStep validacionXsd;
    private final GeneracionXmlWorkflowStep generacionXml;
    private final FirmaElectronicaWorkflowStep firmaElectronica;
    private final EnvioSriWorkflowStep envioSri;
    private final AutorizacionSriWorkflowStep autorizacionSri;\n    private final ComprobanteReprocessService comprobanteReprocessService;

    public ContextoWorkflow crearContexto(ComprobanteGeneracionRequest request) {
        return ContextoWorkflow.nuevo(request);
    }

    public ContextoWorkflow validar(ContextoWorkflow contexto) {
        contexto.registrarResultado(validacionComprobante.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow asignarSecuencial(ContextoWorkflow contexto) {
        contexto.registrarResultado(asignacionSecuencial.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow generarClaveAcceso(ContextoWorkflow contexto) {
        contexto.registrarResultado(generacionClaveAcceso.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow generarXml(ContextoWorkflow contexto) {
        contexto.registrarResultado(generacionXml.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow enviarSri(ContextoWorkflow contexto) {
        contexto.registrarResultado(envioSri.ejecutar(contexto));
        return contexto;
    }

    public void reprocesarAutorizacion(Long comprobanteId) {\n        comprobanteReprocessService.reprocesarAutorizacion(comprobanteId);\n    }\n\n    public ContextoWorkflow autorizarSri(ContextoWorkflow contexto) {
        contexto.registrarResultado(autorizacionSri.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow validarXsd(ContextoWorkflow contexto) {
        contexto.registrarResultado(validacionXsd.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow firmar(ContextoWorkflow contexto) {
        contexto.registrarResultado(firmaElectronica.ejecutar(contexto));
        return contexto;
    }

    public ComprobanteGeneracionResponse respuesta(ContextoWorkflow contexto) {
        var solicitud = contexto.getSolicitud();
        return ComprobanteGeneracionResponse.builder()
                .idTransaccion(solicitud.getIdTransaccion())
                .fechaInicio(solicitud.getFechaInicio())
                .fechaFin(OffsetDateTime.now())
                .exitoso(contexto.getUltimoResultado() == null || contexto.getUltimoResultado().isExitosa())
                .estado(contexto.getUltimoResultado() == null ? null : contexto.getUltimoResultado().getEstado())
                .claveAcceso(contexto.getClaveAcceso())
                .tipoDocumento(solicitud.getTipoDocumento())
                .estadoSri(contexto.getEstadoSri())
                .build();
    }
}
