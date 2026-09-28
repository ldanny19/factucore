package ec.dalara.factucore.notificacion.infrastructure.template;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.stereotype.Service;
import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlantillaCorreoService {
    private final NotificacionProperties properties;
    private final MessageResolver messageResolver;

    public String renderizar(Map<String, String> variables) {
        Path path = Path.of(properties.getPlantilla().getRuta());
        try {
            String contenido = Files.readString(path, StandardCharsets.UTF_8);
            for (var entry : variables.entrySet()) {
                contenido = contenido.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return contenido;
        } catch (Exception e) {
            throw new IllegalStateException(messageResolver.resolver(MessageCodes.PLANTILLA_LECTURA_ERROR, path), e);
        }
    }
}
