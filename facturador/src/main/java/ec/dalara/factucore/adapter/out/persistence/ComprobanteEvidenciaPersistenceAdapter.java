package ec.dalara.factucore.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteEvidenciaPersistenceAdapter implements ComprobanteEvidenciaPort {

    private final ObjectMapper objectMapper;

    @Value("${factucore.path.documentos}")
    private String directorioDocumentos;

    @Override
    public void guardarXmlFirmado(Long comprobanteId, String xmlFirmado) {
        guardarTexto(comprobanteId, "xml-firmado", "xml", xmlFirmado);
    }

    @Override
    public void guardarRespuestaSriRecepcion(Long comprobanteId, SriResponse respuesta) {
        guardarTexto(comprobanteId, "sri-recepcion", "json", serializar(respuesta));
    }

    @Override
    public void guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta) {
        guardarTexto(comprobanteId, "sri-autorizacion", "json", serializar(respuesta));
    }

    @Override
    public void guardarRide(Long comprobanteId, byte[] pdf) {
        if (pdf == null || pdf.length == 0) {
            throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR);
        }
        guardarBytes(comprobanteId, "ride", "pdf", pdf);
    }

    private void guardarTexto(Long comprobanteId, String tipo, String extension, String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }
        guardarBytes(comprobanteId, tipo, extension, contenido.getBytes(StandardCharsets.UTF_8));
    }

    private void guardarBytes(Long comprobanteId, String tipo, String extension, byte[] contenido) {
        if (comprobanteId == null) {
            throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
        }
        try {
            Path directorio = Path.of(directorioDocumentos, "comprobantes", comprobanteId.toString());
            Files.createDirectories(directorio);
            Path archivo = directorio.resolve(tipo + "." + extension);
            Files.write(archivo, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception.getMessage());
        }
    }

    private String serializar(SriResponse respuesta) {
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (JsonProcessingException exception) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA, exception.getMessage());
        }
    }
}
