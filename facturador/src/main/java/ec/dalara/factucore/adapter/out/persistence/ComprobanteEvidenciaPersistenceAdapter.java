package ec.dalara.factucore.adapter.out.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRespuestaSri;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRespuestaSriRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteEvidenciaPersistenceAdapter implements ComprobanteEvidenciaPort {

    private final ComprobanteRepository comprobanteRepository;
    private final ObjectMapper objectMapper;
    private final ComprobanteRespuestaSriRepository respuestaSriRepository;

    @Override
    @Transactional
    public void guardarXmlGenerado(Long comprobanteId, String xml) {
        Comprobante comprobante = obtener(comprobanteId);
        comprobante.setXmlGenerado(xml);
        comprobante.setFechaGeneracionXml(LocalDateTime.now());
        comprobanteRepository.save(comprobante);
    }

    @Override
    @Transactional
    public void guardarXmlFirmado(Long comprobanteId, String xmlFirmado) {
        Comprobante comprobante = obtener(comprobanteId);
        comprobante.setXmlFirmado(xmlFirmado);
        comprobante.setFechaFirma(LocalDateTime.now());
        comprobanteRepository.save(comprobante);
    }

    @Override
    @Transactional
    public void guardarRespuestaSriRecepcion(Long comprobanteId, SriResponse respuesta) {
        Comprobante comprobante = obtener(comprobanteId);
        String evidencia = serializar(respuesta);
        LocalDateTime ahora = LocalDateTime.now();
        comprobante.setRespuestaSriRecepcion(evidencia);
        comprobante.setFechaRespuestaSriRecepcion(ahora);
        comprobanteRepository.save(comprobante);
        guardarHistorial(comprobante, "RECEPCION", respuesta, evidencia, ahora);
    }

    @Override
    @Transactional
    public void guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta) {
        Comprobante comprobante = obtener(comprobanteId);
        String evidencia = serializar(respuesta);
        LocalDateTime ahora = LocalDateTime.now();
        comprobante.setRespuestaSriAutorizacion(evidencia);
        comprobante.setFechaRespuestaSriAutorizacion(ahora);
        comprobanteRepository.save(comprobante);
        guardarHistorial(comprobante, "AUTORIZACION", respuesta, evidencia, ahora);
    }

    private Comprobante obtener(Long comprobanteId) {
        return comprobanteRepository.findByIdAndEstadoRegistro(comprobanteId, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new ApplicationException(
                        MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO));
    }

    private void guardarHistorial(Comprobante comprobante, String tipoRespuesta, SriResponse respuesta,
            String evidencia, LocalDateTime fechaRespuesta) {
        respuestaSriRepository.save(ComprobanteRespuestaSri.builder()
                .comprobante(comprobante)
                .tipoRespuesta(tipoRespuesta)
                .estado(respuesta.estado())
                .identificador(respuesta.identificador())
                .respuesta(evidencia)
                .fechaRespuesta(fechaRespuesta)
                .estadoRegistro(EstadoRegistro.ACTIVO.name())
                .usuarioCreacion("SISTEMA")
                .fechaCreacion(fechaRespuesta)
                .build());
    }

    private String serializar(SriResponse respuesta) {
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (JsonProcessingException exception) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA, exception.getMessage());
        }
    }
}
