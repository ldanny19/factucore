package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReprocessService {

    private final ComprobanteRepository comprobanteRepository;
    private final SriService sriService;
    private final SriProperties sriProperties;
    private final RidePort ridePort;

    @Transactional
    public void reprocesarAutorizacion(Long comprobanteId) {
        Comprobante comprobante = comprobanteRepository.findByIdAndEstadoRegistro(
                comprobanteId, EstadoRegistro.ACTIVO).orElse(null);

        if (comprobante == null || !EstadoProceso.AUTORIZACION_PENDIENTE.name().equals(comprobante.getEstadoProceso())) {
            return;
        }

        try {
            SriResponse respuesta = sriService.autorizar(comprobante.getClaveAcceso());
            comprobante.setNumeroConsultasAutorizacion(comprobante.getNumeroConsultasAutorizacion() + 1);

            if ("AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
                comprobante.setEstadoProceso(EstadoProceso.AUTORIZADO.name());
                comprobante.setNumeroAutorizacion(respuesta.identificador());
                comprobante.setFechaAutorizacion(LocalDateTime.now());
                comprobante.setFechaProximoReproceso(null);
                comprobante.setCodigoError(null);
                comprobante.setMensajeError(null);

                byte[] pdf = ridePort.generar(comprobante);
                comprobante.setArchivoPdf(pdf);
                comprobante.setEstadoProceso(EstadoProceso.RIDE_GENERADO.name());
            } else if ("NO AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
                comprobante.setEstadoProceso(EstadoProceso.ERROR.name());
                comprobante.setFechaProximoReproceso(null);
                if (!respuesta.mensajes().isEmpty()) {
                    comprobante.setCodigoError(respuesta.mensajes().get(0).identificador());
                    comprobante.setMensajeError(respuesta.mensajes().get(0).mensaje());
                }
            } else if ("EN PROCESO".equalsIgnoreCase(respuesta.estado())) {
                int maxConsultas = Math.max(sriProperties.getAutorizacion().getMaxConsultas(), 1);
                if (comprobante.getNumeroConsultasAutorizacion() >= maxConsultas) {
                    comprobante.setEstadoProceso(EstadoProceso.ERROR.name());
                    comprobante.setFechaProximoReproceso(null);
                    comprobante.setCodigoError(MessageCodes.SRI_MAX_CONSULTAS_AUTORIZACION);
                } else {
                    long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaConsultaMs(), 1000);
                    comprobante.setFechaProximoReproceso(
                            LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
                }
            } else {
                comprobante.setEstadoProceso(EstadoProceso.ERROR.name());
                comprobante.setFechaProximoReproceso(null);
                comprobante.setCodigoError(MessageCodes.SRI_ESTADO_AUTORIZACION_NO_RECONOCIDO);
                comprobante.setMensajeError(respuesta.estado());
            }

            comprobanteRepository.save(comprobante);
        } catch (RuntimeException exception) {
            comprobante.setEstadoProceso(EstadoProceso.AUTORIZACION_PENDIENTE.name());
            long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaConsultaMs(), 1000);
            comprobante.setFechaProximoReproceso(
                    LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
            comprobante.setCodigoError(MessageCodes.SRI_ERROR_COMUNICACION);
            comprobante.setMensajeError(exception.getMessage());
            comprobanteRepository.save(comprobante);
        }
    }
}
