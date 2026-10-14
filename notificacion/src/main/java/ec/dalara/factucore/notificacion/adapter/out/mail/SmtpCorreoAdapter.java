package ec.dalara.factucore.notificacion.adapter.out.mail;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ec.dalara.factucore.notificacion.application.port.out.CorreoPort;
import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmtpCorreoAdapter implements CorreoPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpCorreoAdapter.class);
    private final JavaMailSender mailSender;
    private final NotificacionProperties properties;
    private final MessageResolver messageResolver;

    @Override
    public void enviar(String destinatario, String asunto, String contenidoHtml, AdjuntoCorreo... adjuntos) {
        LOGGER.info("Preparando correo para envío SMTP. ADJUNTOS={}.", adjuntos == null ? 0 : adjuntos.length);
        try {
            var mensaje = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(Objects.requireNonNull(properties.getCorreo().getRemitente()));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            if (adjuntos != null) {
                for (var adjunto : adjuntos) {
                    helper.addAttachment(adjunto.nombre(), new ByteArrayResource(adjunto.contenido()), adjunto.tipoContenido());
                }
            }
            mailSender.send(mensaje);
            LOGGER.info("Servidor SMTP aceptó la solicitud de envío de correo.");
        } catch (Exception e) {
            LOGGER.error("Falló el envío del correo mediante SMTP.", e);
            throw new IllegalStateException(messageResolver.resolver(MessageCodes.CORREO_ENVIO_ERROR), e);
        }
    }
}
