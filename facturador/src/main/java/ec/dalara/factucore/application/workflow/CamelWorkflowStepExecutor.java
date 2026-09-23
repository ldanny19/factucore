package ec.dalara.factucore.application.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public ContextoWorkflow verificarIdempotencia(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getSolicitud() == null) return contexto;
        comprobanteService.obtenerPorEmpresaEIdTransaccion(contexto.getSolicitud().getIdEmpresa(), contexto.getSolicitud().getIdTransaccion()).ifPresent(c -> {
            contexto.asignarComprobante(c);
            contexto.setClaveAcceso(c.getClaveAcceso());
            contexto.setSecuencial(c.getSecuencial());
            contexto.marcarIdempotente();
        });
        return contexto;
    }

    public ContextoWorkflow validar(ContextoWorkflow contexto) {
        registrarResultado(contexto, validacion.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow asignarSecuencial(ContextoWorkflow contexto) {
        registrarResultado(contexto, asignacionSecuencial.ejecutar(contexto));
        return contexto;
    }

    @Transactional
    public ContextoWorkflow reservarComprobante(ContextoWorkflow contexto) {
        if (contexto == null || contexto.isIdempotente()) {
            return contexto;
        }

        asignarSecuencial(contexto);

        if (contexto.isIdempotente()) {
            return contexto;
        }

        generarClaveAcceso(contexto);
        persistirComprobante(contexto);
        return contexto;
    }

    public ContextoWorkflow generarClaveAcceso(ContextoWorkflow contexto) {
        registrarResultado(contexto, generacionClaveAcceso.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow persistirComprobante(ContextoWorkflow contexto) {
        registrarResultado(contexto, persistenciaComprobante.ejecutar(contexto));
        return contexto;
    }

    public ContextoWorkflow generarXml(ContextoWorkflow contexto) {
        registrarResultado(contexto, generacionXml.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow validarXsd(ContextoWorkflow contexto) {
        registrarResultado(contexto, validacionXsd.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow firmar(ContextoWorkflow contexto) {
        registrarResultado(contexto, firmaElectronica.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow enviarSri(ContextoWorkflow contexto) {
        registrarResultado(contexto, envioSri.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow autorizarSri(ContextoWorkflow contexto) {
        registrarResultado(contexto, autorizacionSri.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow generarRide(ContextoWorkflow contexto) {
        registrarResultado(contexto, generacionRide.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    public ContextoWorkflow publicarNotificacion(ContextoWorkflow contexto) {
        registrarResultado(contexto, notificacion.ejecutar(contexto));
        persistirCambios(contexto);
        return contexto;
    }

    private void registrarResultado(ContextoWorkflow contexto, ec.dalara.factucore.domain.workflow.ResultadoEtapa resultado) {
        contexto.registrarResultado(resultado);

        if (contexto.getComprobante() != null) {
            if (!resultado.isExitosa()) {
                contexto.getComprobante().setCodigoError(resultado.getCodigoError());
                contexto.getComprobante().setMensajeError(resultado.getMensaje());
            } else if (!EstadoProceso.ERROR.name().equals(resultado.getEstado())) {
                contexto.getComprobante().setCodigoError(null);
                contexto.getComprobante().setMensajeError(null);
            }
        }
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
