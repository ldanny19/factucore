package ec.dalara.factucore.notificacion.application.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.messaging.api.EventoMensaje;
import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import ec.dalara.factucore.notificacion.application.port.out.CorreoPort;
import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio;
import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio.EstadoEnvio;
import ec.dalara.factucore.notificacion.infrastructure.template.PlantillaCorreoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionService.class);

    private final CorreoPort correoPort;
    private final PlantillaCorreoService plantillaCorreoService;
    private final NotificacionProperties properties;
    private final MessageResolver messageResolver;
    private final NotificacionPersistenciaService persistenciaService;
    private final ObjectMapper objectMapper;

    public void notificar(EventoMensaje evento, ComprobanteAutorizado comprobante) {
        if (evento == null || comprobante == null) {
            throw new IllegalArgumentException(messageResolver.resolver(MessageCodes.COMPROBANTE_REQUERIDO));
        }

        validar(comprobante);
        NotificacionEnvio envio = persistenciaService.registrar(evento, comprobante, serializar(evento),
                properties.getCorreo().getAsunto());

        if (envio.getEstadoEnvio() == EstadoEnvio.ENVIADA || envio.getEstadoEnvio() == EstadoEnvio.FALLIDA
                || envio.getEstadoEnvio() == EstadoEnvio.EN_PROCESO) {
            LOGGER.debug("El evento de notificación {} ya se encuentra en estado {}.", evento.id(),
                    envio.getEstadoEnvio());
            return;
        }

        String contenido = envio.getContenidoHtml();
        if (contenido == null) {
            contenido = plantillaCorreoService.renderizar(Map.of(
                    "nombreCliente", valor(comprobante.nombreCliente()),
                    "nombreEmpresa", valor(comprobante.nombreEmpresa()),
                    "fechaComprobante",
                    comprobante.fechaComprobante() == null ? "" : comprobante.fechaComprobante().toString()));
            persistenciaService.guardarContenidoHtml(envio.getId(), contenido);
        }

        if (!persistenciaService.marcarEnProceso(envio.getId())) {
            return;
        }

        Path xml = Path.of(comprobante.rutaXmlAutorizado());
        Path ride = Path.of(comprobante.rutaRide());
        int maxIntentos = Math.max(properties.getReintentos().getMaxIntentos(), 1);

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                correoPort.enviar(comprobante.correo(), properties.getCorreo().getAsunto(), contenido,
                        new CorreoPort.AdjuntoCorreo(nombreZip(xml), "application/zip", comprimirXml(xml)),
                        new CorreoPort.AdjuntoCorreo(ride.getFileName().toString(), "application/pdf", leer(ride)));
            } catch (RuntimeException exception) {
                boolean definitivo = intento >= maxIntentos;
                String respuestaError = messageResolver.resolver(MessageCodes.CORREO_ENVIO_ERROR)
                        + (exception.getMessage() == null ? "" : " " + exception.getMessage());
                persistenciaService.registrarIntento(envio.getId(), respuestaError, false, definitivo);
                if (definitivo) {
                    LOGGER.error("Se agotaron los intentos de notificación. ID_EVENTO={}, ID_TRANSACCION={}.",
                            evento.id(), comprobante.idTransaccion(), exception);
                    return;
                }

                LOGGER.warn("Falló el envío de notificación. ID_EVENTO={}, intento {} de {}.",
                        evento.id(), intento, maxIntentos, exception);
                esperar(properties.getReintentos().getEsperaReintentoMs());
                continue;
            }

            // Si falla la persistencia posterior al SMTP, no se repite el envío:
            // el resultado externo es ambiguo y el registro queda en EN_PROCESO.
            persistenciaService.registrarIntento(envio.getId(),
                    "SMTP aceptó la solicitud de envío; la entrega final no está confirmada.", true, false);
            LOGGER.info("Notificación enviada a SMTP. ID_EVENTO={}, ID_TRANSACCION={}, INTENTO={}.",
                    evento.id(), comprobante.idTransaccion(), intento);
            return;
        }
    }

    public void procesarPendiente(NotificacionEnvio envio) {
        if (envio == null || envio.getEstadoEnvio() != EstadoEnvio.PENDIENTE) {
            return;
        }
        try {
            EventoMensaje evento = objectMapper.readValue(envio.getEventoJson(), EventoMensaje.class);
            ComprobanteAutorizado comprobante = objectMapper.treeToValue(evento.payload(),
                    ComprobanteAutorizado.class);
            notificar(evento, comprobante);
        } catch (Exception exception) {
            LOGGER.error("No fue posible recuperar el evento pendiente {}.", envio.getIdEvento(), exception);
            persistenciaService.registrarIntento(envio.getId(),
                    messageResolver.resolver(MessageCodes.EVENTO_PROCESAMIENTO_ERROR, envio.getIdEvento()),
                    false, true);
        }
    }

    private String serializar(EventoMensaje evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    messageResolver.resolver(MessageCodes.EVENTO_SERIALIZACION_ERROR, evento.id()), exception);
        }
    }

    private void esperar(long esperaMs) {
        if (esperaMs <= 0) {
            return;
        }

        try {
            Thread.sleep(esperaMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    messageResolver.resolver(MessageCodes.REINTENTO_INTERRUPCION), exception);
        }
    }

    private void validar(ComprobanteAutorizado comprobante) {
        if (!StringUtils.hasText(comprobante.correo())
                || !StringUtils.hasText(comprobante.rutaXmlAutorizado())
                || !StringUtils.hasText(comprobante.rutaRide())) {
            throw new IllegalArgumentException(messageResolver.resolver(MessageCodes.DATOS_REQUERIDOS));
        }
    }

    private byte[] comprimirXml(Path path) {
        try (var out = new java.io.ByteArrayOutputStream(); var zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(path.getFileName().toString()));
            zip.write(leer(path));
            zip.closeEntry();
            return out.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    messageResolver.resolver(MessageCodes.XML_COMPRESION_ERROR), exception);
        }
    }

    private byte[] leer(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    messageResolver.resolver(MessageCodes.ARCHIVO_LECTURA_ERROR, path), exception);
        }
    }

    private String nombreZip(Path path) {
        String nombre = path.getFileName().toString();
        return nombre.endsWith(".xml") ? nombre.substring(0, nombre.length() - 4) + ".zip" : nombre + ".zip";
    }

    private String valor(String valor) {
        return valor == null ? "" : valor;
    }
}
