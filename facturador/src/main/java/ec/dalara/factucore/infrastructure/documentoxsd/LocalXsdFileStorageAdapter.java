package ec.dalara.factucore.infrastructure.documentoxsd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.XsdFileStoragePort;
import ec.dalara.factucore.infrastructure.InfrastructureException;

@Component
public class LocalXsdFileStorageAdapter implements XsdFileStoragePort {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalXsdFileStorageAdapter.class);
	private static final String ERROR_GUARDAR = "FACTUCORE.XSD.ARCHIVO.GUARDAR.ERROR";
	private static final String ERROR_ELIMINAR = "FACTUCORE.XSD.ARCHIVO.ELIMINAR.ERROR";

	private final Path directorioBase;

	public LocalXsdFileStorageAdapter(@Value("${factucore.path.xsd}") String directorioBase) {
		this.directorioBase = Path.of(directorioBase).toAbsolutePath().normalize();
	}

	@Override
	public Path guardar(String carpetaTipoDocumento, String nombreArchivo, byte[] contenido) {
		if (nombreArchivo == null || !nombreArchivo.toLowerCase(java.util.Locale.ROOT).endsWith(".xsd")) {
			throw new InfrastructureException("FACTUCORE.XSD.ARCHIVO.EXTENSION.INVALIDA");
		}
		if (contenido == null || contenido.length == 0) {
			throw new InfrastructureException("FACTUCORE.XSD.ARCHIVO.REQUERIDO");
		}

		Path archivo = directorioBase.resolve(Path.of(nombreArchivo).getFileName()).normalize();
		if (!archivo.startsWith(directorioBase)) {
			throw new InfrastructureException(ERROR_GUARDAR);
		}

		try {
			Files.createDirectories(directorioBase);
			Files.write(archivo, contenido, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
			return archivo;
		} catch (IOException exception) {
			throw new InfrastructureException(ERROR_GUARDAR, exception);
		}
	}

	@Override
	public void eliminar(Path ruta) {
		if (ruta == null) {
			return;
		}

		Path normalizada = ruta.toAbsolutePath().normalize();
		if (!normalizada.startsWith(directorioBase)) {
			throw new InfrastructureException(ERROR_ELIMINAR);
		}

		try {
			Files.deleteIfExists(normalizada);
		} catch (IOException exception) {
			LOGGER.error("No se pudo eliminar el XSD almacenado tras revertir la transacción: {}", normalizada,
					exception);
			throw new InfrastructureException(ERROR_ELIMINAR, exception);
		}
	}
}
