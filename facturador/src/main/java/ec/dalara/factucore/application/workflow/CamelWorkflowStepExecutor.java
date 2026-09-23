package ec.dalara.factucore.application.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.application.contract.response.ResultadoResponse;
import ec.dalara.factucore.application.service.ComprobanteReprocessService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import lombok.RequiredArgsConstructor;

@Component("camelWorkflowStepExecutor")
@RequiredArgsConstructor
public class CamelWorkflowStepExecutor {

    private final ValidacionComprobanteWorkflowStep validacion;
    private final AsignacionSecuencialWorkflowStep asignacionSecuencial;
    private final GeneracionClaveAccesoWorkflowStep generacionClaveAcceso;
    private final PersistenciaComprobanteWorkflowStep persistenciaComprobante;
    private final GeneracionXmlWorkflowStep generacionXml;
    private final ValidacionXsdWorkflowStep validacionXsd;
    private final FirmaElectronicaWorkflowStep firmaElectronica;
    private final EnvioSriWorkflowStep envioSri;
    private final AutorizacionSriWorkflowStep autorizacionSri;
    private final GeneracionRideWorkflowStep generacionRide;
    private final NotificacionWorkflowStep notificacion;
    private final ComprobanteReprocessService reprocessService;
    private final ComprobanteService comprobanteService;

    public ContextoWorkflow crearContexto(ComprobanteGeneracionRequest request) {
        return ContextoWorkflow.nuevo(request);
    }

    public ContextoWorkflow validar(ContextoWorkflow contexto) {
        contexto.registrarResultado(validacion.ejecutar(contexto));
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

    public ContextoWorkflow persistirComprobante(ContextoWorkflow contexto) {
        contexto.registrarResultado(persistenciaComprobante.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow generarXml(ContextoWorkflow contexto) {
        contexto.registrarResultado(generacionXml.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow validarXsd(ContextoWorkflow contexto) {
        contexto.registrarResultado(validacionXsd.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow firmar(ContextoWorkflow contexto) {
        contexto.registrarResultado(firmaElectronica.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow enviarSri(ContextoWorkflow contexto) {
        contexto.registrarResultado(envioSri.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow autorizarSri(ContextoWorkflow contexto) {
        contexto.registrarResultado(autorizacionSri.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow generarRide(ContextoWorkflow contexto) {
        contexto.registrarResultado(generacionRide.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow publicarNotificacion(ContextoWorkflow contexto) {
        contexto.registrarResultado(notificacion.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    private void persistirCambios(ContextoWorkflow contexto) {
        if (contexto.getComprobante() != null) {
            comprobanteService.guardar(contexto.getComprobante());
        }
    }

    public ComprobanteGeneracionResponse respuesta(ContextoWorkflow contexto) {
        var comprobante = contexto.getComprobante();
        var request = contexto.getSolicitud();

        String numeroComprobante = comprobante == null ? null :
                comprobante.getCodigoDocumento() + "-" +
                comprobante.getEstablecimiento().getCodigo() + "-" +
                comprobante.getPuntoEmision().getCodigo() + "-" +
                comprobante.getSecuencial();

        boolean exitoso = comprobante != null &&
                (EstadoProceso.RIDE_GENERADO.name().equals(comprobante.getEstadoProceso())
                || EstadoProceso.AUTORIZADO.name().equals(comprobante.getEstadoProceso()));

        return ComprobanteGeneracionResponse.builder()
                .idTransaccion(request == null ? null : request.getIdTransaccion())
                .fechaInicio(contexto.getFechaInicio().atOffset(ZoneOffset.UTC))
                .fechaFin(OffsetDateTime.now(ZoneOffset.UTC))
                .exitoso(exitoso)
                .estado(comprobante == null ? null : comprobante.getEstadoProceso())
                .resultado(ResultadoResponse.builder().build())
                .claveAcceso(contexto.getClaveAcceso())
                .numeroComprobante(numeroComprobante)
                .tipoDocumento(request == null ? null : request.getTipoDocumento())
                .estadoSri(contexto.getEstadoSri())
                .archivoPdf(contexto.getRide())
                .build();
    }

    public void reprocesarAutorizacion(Long comprobanteId) {
        reprocessService.reprocesarAutorizacion(comprobanteId);
    }
}
