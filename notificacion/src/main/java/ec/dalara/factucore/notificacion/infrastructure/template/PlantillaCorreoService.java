package ec.dalara.factucore.notificacion.infrastructure.template;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlantillaCorreoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlantillaCorreoService.class);
	private final NotificacionProperties properties;
	private final MessageResolver messageResolver;

	public String renderizar(Map<String, String> variables) {
		Path path = Path.of(properties.getPlantilla().getRuta());
		LOGGER.info("Inicia renderizado de plantilla de correo. VARIABLES={}, ARCHIVO={}.", variables.size(), path);
		try {
			String contenido = Files.readString(path, StandardCharsets.UTF_8);
			for (var entry : variables.entrySet()) {
				contenido = contenido.replace("{{" + entry.getKey() + "}}", entry.getValue());
			}
			LOGGER.info("Plantilla de correo renderizada correctamente. VARIABLES={}.", variables.size());
			return contenido;
		} catch (Exception e) {
			LOGGER.error("No fue posible renderizar la plantilla de correo. ARCHIVO={}.", path, e);
			throw new IllegalStateException(messageResolver.resolver(MessageCodes.PLANTILLA_LECTURA_ERROR, path), e);
		}
	}
}
