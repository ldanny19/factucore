package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowResultadoService {

    private final ComprobanteAuditoriaService comprobanteAuditoriaService;
    private final ComprobanteService comprobanteService;
    private final PersistenciaSeccionesComprobanteService persistenciaSeccionesComprobanteService;

    @Transactional
    public String registrar(ContextoWorkflow contexto, ResultadoEtapa resultado) {
        contexto.registrarResultado(resultado);
        persistir(contexto, resultado);
        return resultado.salida();
    }

    private void persistir(ContextoWorkflow contexto, ResultadoEtapa resultado) {
        String estadoAnterior = contexto.getComprobante() == null ? null
                : contexto.getComprobante().getEstadoProceso();
        LocalDateTime fechaInicio = LocalDateTime.now();

        if (contexto.getComprobante() != null) {
            if (!resultado.isExitosa()) {
                contexto.getComprobante().setCodigoError(resultado.getCodigoError());
                contexto.getComprobante().setMensajeError(resultado.getMensaje());
            } else {
                contexto.getComprobante().setCodigoError(null);
                contexto.getComprobante().setMensajeError(null);
            }
            contexto.asignarComprobante(comprobanteService.guardar(contexto.getComprobante()));

            if (resultado.isExitosa() && contexto.getSolicitud() != null) {
                JsonNode datos = contexto.getSolicitud().getDatos();
                persistenciaSeccionesComprobanteService.persistir(contexto.getComprobante(), datos,
                        contexto.getSolicitud().getUsuario());
            }
        }

        if (contexto.getComprobanteId() != null && resultado.getEtapa() != null) {
            comprobanteAuditoriaService.registrarResultado(contexto.getComprobanteId(), resultado.getEtapa().name(),
                    estadoAnterior, resultado, fechaInicio, LocalDateTime.now());
        }
    }
}
