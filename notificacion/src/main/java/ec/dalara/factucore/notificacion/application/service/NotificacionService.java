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

import ec.dalara.factucore.messaging.api.notificacion.ComprobanteAutorizado;
import ec.dalara.factucore.notificacion.application.port.out.CorreoPort;
import ec.dalara.factucore.notificacion.infrastructure.config.NotificacionProperties;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageCodes;
import ec.dalara.factucore.notificacion.infrastructure.message.MessageResolver;
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

	public void notificar(ComprobanteAutorizado comprobante) {
		if (comprobante == null) {
			throw new IllegalArgumentException(messageResolver.resolver(MessageCodes.COMPROBANTE_REQUERIDO));
		}

		validar(comprobante);
		Path xml = Path.of(comprobante.rutaXmlAutorizado());
		Path ride = Path.of(comprobante.rutaRide());
		String contenido = plantillaCorreoService.renderizar(Map.of("nombreCliente", valor(comprobante.nombreCliente()),
				"nombreEmpresa", valor(comprobante.nombreEmpresa()), "fechaComprobante",
				comprobante.fechaComprobante() == null ? "" : comprobante.fechaComprobante().toString()));

		int maxIntentos = Math.max(properties.getReintentos().getMaxIntentos(), 1);
		for (int intento = 1; intento <= maxIntentos; intento++) {
			try {
				correoPort.enviar(comprobante.correo(), properties.getCorreo().getAsunto(), contenido,
						new CorreoPort.AdjuntoCorreo(nombreZip(xml), "application/zip", comprimirXml(xml)),
						new CorreoPort.AdjuntoCorreo(ride.getFileName().toString(), "application/pdf", leer(ride)));
				return;
			} catch (RuntimeException exception) {
				if (intento >= maxIntentos) {
					throw exception;
				}

				LOGGER.warn("Falló el envío de notificación. Intento {} de {}.", intento, maxIntentos, exception);
				esperar(properties.getReintentos().getEsperaReintentoMs());
			}
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
			throw new IllegalStateException("Se interrumpió la espera entre intentos de notificación.", exception);
		}
	}

	private void validar(ComprobanteAutorizado comprobante) {
		if (!StringUtils.hasText(comprobante.correo()) || !StringUtils.hasText(comprobante.rutaXmlAutorizado())
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
		} catch (Exception e) {
			throw new IllegalStateException(messageResolver.resolver(MessageCodes.XML_COMPRESION_ERROR), e);
		}
	}

	private byte[] leer(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (Exception e) {
			throw new IllegalStateException(messageResolver.resolver(MessageCodes.ARCHIVO_LECTURA_ERROR, path), e);
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
