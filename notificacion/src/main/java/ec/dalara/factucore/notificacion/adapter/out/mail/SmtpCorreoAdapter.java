package ec.dalara.factucore.notificacion.adapter.out.mail;

import java.util.Objects;
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
    private final JavaMailSender mailSender;
    private final NotificacionProperties properties;
    private final MessageResolver messageResolver;

    @Override
    public void enviar(String destinatario, String asunto, String contenidoHtml, AdjuntoCorreo... adjuntos) {
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
        } catch (Exception e) {
            throw new IllegalStateException(messageResolver.resolver(MessageCodes.CORREO_ENVIO_ERROR), e);
        }
    }
}
