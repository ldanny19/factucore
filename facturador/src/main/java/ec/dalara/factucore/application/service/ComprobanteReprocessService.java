package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteReprocessService {

    private static final String ESTADO_AUTORIZACION_PENDIENTE = "AUTORIZACION_PENDIENTE";
    private static final String ESTADO_AUTORIZADO = "AUTORIZADO";
    private static final String ESTADO_NO_AUTORIZADO = "NO_AUTORIZADO";
    private static final String ESTADO_ERROR_AUTORIZACION = "ERROR_AUTORIZACION";

    private final ComprobanteRepository comprobanteRepository;
    private final SriService sriService;
    private final SriProperties sriProperties;

    @Transactional
    public void reprocesarAutorizacion(Long comprobanteId) {
        Comprobante comprobante = comprobanteRepository.findByIdAndEstadoRegistro(
                comprobanteId, EstadoRegistro.ACTIVO).orElse(null);

        if (comprobante == null || !ESTADO_AUTORIZACION_PENDIENTE.equals(comprobante.getEstadoProceso())) {
            return;
        }

        try {
            SriResponse respuesta = sriService.autorizar(comprobante.getClaveAcceso());
            comprobante.setNumeroConsultasAutorizacion(comprobante.getNumeroConsultasAutorizacion() + 1);

            if ("AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
                comprobante.setEstadoProceso(ESTADO_AUTORIZADO);
                comprobante.setNumeroAutorizacion(respuesta.identificador());
                comprobante.setFechaAutorizacion(LocalDateTime.now());
                comprobante.setFechaProximoReproceso(null);
                comprobante.setCodigoError(null);
                comprobante.setMensajeError(null);
            } else if ("NO AUTORIZADO".equalsIgnoreCase(respuesta.estado())) {
                comprobante.setEstadoProceso(ESTADO_NO_AUTORIZADO);
                comprobante.setFechaProximoReproceso(null);
                if (!respuesta.mensajes().isEmpty()) {
                    comprobante.setCodigoError(respuesta.mensajes().get(0).identificador());
                    comprobante.setMensajeError(respuesta.mensajes().get(0).mensaje());
                }
            } else if ("EN PROCESO".equalsIgnoreCase(respuesta.estado())) {
                int maxConsultas = Math.max(sriProperties.getAutorizacion().getMaxConsultas(), 1);
                if (comprobante.getNumeroConsultasAutorizacion() >= maxConsultas) {
                    comprobante.setEstadoProceso(ESTADO_ERROR_AUTORIZACION);
                    comprobante.setFechaProximoReproceso(null);
                    comprobante.setCodigoError(MessageCodes.SRI_MAX_CONSULTAS_AUTORIZACION);
                    comprobante.setMensajeError(null);
                } else {
                    long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaConsultaMs(), 1000);
                    comprobante.setFechaProximoReproceso(LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
                }
            } else {
                comprobante.setEstadoProceso(ESTADO_ERROR_AUTORIZACION);
                comprobante.setFechaProximoReproceso(null);
                comprobante.setCodigoError(MessageCodes.SRI_ESTADO_AUTORIZACION_NO_RECONOCIDO);
                comprobante.setMensajeError(respuesta.estado());
            }

            comprobanteRepository.save(comprobante);
        } catch (RuntimeException exception) {
            comprobante.setEstadoProceso(ESTADO_AUTORIZACION_PENDIENTE);
            long esperaMs = Math.max(sriProperties.getAutorizacion().getEsperaConsultaMs(), 1000);
            comprobante.setFechaProximoReproceso(LocalDateTime.now().plusNanos(esperaMs * 1_000_000));
            comprobante.setCodigoError(MessageCodes.SRI_ERROR_COMUNICACION);
            comprobante.setMensajeError(exception.getMessage());
            comprobanteRepository.save(comprobante);
        }
    }
}
