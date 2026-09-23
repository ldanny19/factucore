package ec.dalara.factucore.application.workflow;

import java.util.Map;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.application.service.SriService;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
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
    private final SriProperties sriProperties;

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

        if ("AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
            contexto.setNumeroAutorizacion(respuesta.identificador());
            contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZADO.name());
            contexto.getComprobante().setFechaProximoReproceso(null);
            contexto.getComprobante().setNumeroAutorizacion(respuesta.identificador());
            return ResultadoEtapa.exitosa(etapa(), EstadoProceso.AUTORIZADO.name(),
                    Map.of("numeroAutorizacion", respuesta.identificador() == null ? "" : respuesta.identificador()));
        }

        if ("EN PROCESO".equalsIgnoreCase(respuesta.estado())) {
            long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaConsultaMs(), 1000);
            contexto.getComprobante().setEstadoProceso(EstadoProceso.AUTORIZACION_PENDIENTE.name());
            contexto.getComprobante().setFechaProximoReproceso(java.time.LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
            return ResultadoEtapa.exitosa(etapa(), EstadoProceso.AUTORIZACION_PENDIENTE.name());
        }

        contexto.getComprobante().setEstadoProceso(EstadoProceso.ERROR.name());
        contexto.getComprobante().setFechaProximoReproceso(null);
        var mensaje = respuesta.mensajes().isEmpty() ? null : respuesta.mensajes().get(0);
        return ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(),
                mensaje == null || mensaje.identificador() == null ? MessageCodes.SRI_RESPUESTA_INVALIDA : mensaje.identificador(),
                mensaje == null ? null : mensaje.mensaje());
    }
}
