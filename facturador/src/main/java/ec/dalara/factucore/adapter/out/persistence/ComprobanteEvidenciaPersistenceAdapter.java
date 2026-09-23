package ec.dalara.factucore.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.sri.SriResponse;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteEvidencia;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteEvidenciaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComprobanteEvidenciaPersistenceAdapter implements ComprobanteEvidenciaPort {

    private static final DateTimeFormatter RESPALDO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    private final ComprobanteRepository comprobanteRepository;
    private final ComprobanteEvidenciaRepository evidenciaRepository;

    @Value("${factucore.path.documentos-xml}")
    private String directorioXml;

    @Value("${factucore.path.documentos-pdf}")
    private String directorioPdf;

    @Override
    @Transactional
    public String guardarXmlGenerado(Long comprobanteId, String xmlGenerado, String usuario) {
        return guardarTexto(comprobanteId, "XML_GENERADO", xmlGenerado, directorioXml, "xml", usuario);
    }

    @Override
    @Transactional
    public String guardarXmlFirmado(Long comprobanteId, String xmlFirmado, String usuario) {
        return guardarTexto(comprobanteId, "XML_FIRMADO", xmlFirmado, directorioXml, "xml", usuario);
    }

    @Override
    @Transactional
    public String guardarRespuestaSriAutorizacion(Long comprobanteId, SriResponse respuesta, String usuario) {
        if (respuesta == null || respuesta.respuestaXml() == null || respuesta.respuestaXml().isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }

        String tipo = "AUTORIZADO".equalsIgnoreCase(respuesta.estado())
                ? "XML_AUTORIZADO"
                : "XML_NO_AUTORIZADO";

        return guardarTexto(comprobanteId, tipo, respuesta.respuestaXml(), directorioXml, "xml", usuario);
    }

    @Override
    @Transactional
    public String guardarRide(Long comprobanteId, byte[] pdf, String usuario) {
        if (pdf == null || pdf.length == 0) {
            throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR);
        }
        return guardarBytes(comprobanteId, "RIDE", pdf, directorioPdf, "pdf", usuario);
    }

    private String guardarTexto(Long comprobanteId, String tipo, String contenido,
            String directorioBase, String extension, String usuario) {
        if (contenido == null || contenido.isBlank()) {
            throw new ApplicationException(MessageCodes.SRI_RESPUESTA_INVALIDA);
        }
        return guardarBytes(comprobanteId, tipo, contenido.getBytes(StandardCharsets.UTF_8),
                directorioBase, extension, usuario);
    }

    private String guardarBytes(Long comprobanteId, String tipo, byte[] contenido,
            String directorioBase, String extension, String usuario) {

        Comprobante comprobante = obtenerComprobante(comprobanteId);
        String prefijo = comprobante.getDocumentoXsd().getPrefijoArchivo();

        if (prefijo == null || prefijo.isBlank()) {
            throw new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA);
        }

        String numeroDocumento = comprobante.getEstablecimiento().getCodigo()
                + comprobante.getPuntoEmision().getCodigo()
                + comprobante.getSecuencial();

        String nombreArchivo = prefijo + "_" + numeroDocumento + "." + extension;
        Path directorio = resolverDirectorio(directorioBase, tipo);
        Path archivo = directorio.resolve(nombreArchivo);

        try {
            Files.createDirectories(directorio);
            Path respaldo = respaldarSiExiste(archivo);

            ComprobanteEvidencia anterior = evidenciaRepository
                    .findByComprobanteIdAndTipoEvidenciaAndActualTrue(comprobanteId, tipo)
                    .orElse(null);

            if (anterior != null) {
                if (respaldo != null) {
                    anterior.setRutaArchivo(respaldo.toString());
                    anterior.setNombreArchivo(respaldo.getFileName().toString());
                }
                anterior.setActual(false);
                anterior.setUsuarioModificacion(usuario);
                anterior.setFechaModificacion(LocalDateTime.now());
                evidenciaRepository.save(anterior);
            }

            Files.write(archivo, contenido);

            LocalDateTime ahora = LocalDateTime.now();
            evidenciaRepository.save(ComprobanteEvidencia.builder()
                    .comprobante(comprobante)
                    .tipoEvidencia(tipo)
                    .nombreArchivo(nombreArchivo)
                    .rutaArchivo(archivo.toString())
                    .hashSha256(calcularHash(contenido))
                    .actual(true)
                    .fechaGeneracion(ahora)
                    .estadoRegistro(EstadoRegistro.ACTIVO)
                    .usuarioCreacion(usuario)
                    .fechaCreacion(ahora)
                    .build());

            return archivo.toString();
        } catch (IOException exception) {
            throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception.getMessage());
        }
    }

    private Path resolverDirectorio(String base, String tipo) {
        return switch (tipo) {
            case "XML_GENERADO" -> Path.of(base, "generado");
            case "XML_FIRMADO" -> Path.of(base, "firmado");
            case "XML_AUTORIZADO" -> Path.of(base, "autorizado");
            case "XML_NO_AUTORIZADO" -> Path.of(base, "no-autorizado");
            case "RIDE" -> Path.of(base);
            default -> throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR);
        };
    }

    private Path respaldarSiExiste(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            return null;
        }

        String nombre = archivo.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        String extension = punto > 0 ? nombre.substring(punto) : "";

        Path respaldo = archivo.resolveSibling(
                base + "_" + LocalDateTime.now().format(RESPALDO_FORMATTER) + extension);

        Files.move(archivo, respaldo, StandardCopyOption.REPLACE_EXISTING);
        return respaldo;
    }

    private Comprobante obtenerComprobante(Long comprobanteId) {
        if (comprobanteId == null) {
            throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
        }

        return comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO));
    }

    private String calcularHash(byte[] contenido) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(contenido);
            StringBuilder resultado = new StringBuilder(64);
            for (byte valor : digest) {
                resultado.append(String.format("%02x", valor));
            }
            return resultado.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}