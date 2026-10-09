package ec.dalara.factucore.application.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.port.out.XsdDefinitionPersistencePort;
import ec.dalara.factucore.application.port.out.XsdParserPort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;
import ec.dalara.factucore.infrastructure.InfrastructureException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class XsdImportService {

	private final XsdParserPort parser;
	private final XsdDefinitionPersistencePort persistence;

	@Value("${factucore.path.xsd}")
	private String directorioXsd;

	public XsdImportResult importar(InputStream inputStream, String systemId, XsdImportRequest request) {
		if (inputStream == null) {
			throw new InfrastructureException("FACTUCORE.XSD.ARCHIVO.REQUERIDO");
		}
		try {
			byte[] contenido = inputStream.readAllBytes();
			XsdDefinitionSource definition = parser.parse(new java.io.ByteArrayInputStream(contenido), systemId);
			Path directorio = Path.of(directorioXsd).toAbsolutePath().normalize();
			Files.createDirectories(directorio);
			String nombre = request.nombreArchivo() == null || request.nombreArchivo().isBlank()
					? request.codigo() + "_" + request.version() + ".xsd"
					: Path.of(request.nombreArchivo()).getFileName().toString();
			nombre = nombre.replaceAll("[^A-Za-z0-9._-]", "_");
			if (!nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".xsd")) {
				nombre += ".xsd";
			}
			Path destino = directorio.resolve(request.codigo() + "_" + request.version() + "_" + nombre)
					.normalize().toAbsolutePath();
			if (!destino.startsWith(directorio)) {
				throw new InfrastructureException("FACTUCORE.XSD.RUTA.INVALIDA");
			}
			Files.write(destino, contenido, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
			String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
			return persistence.persist(request, definition, destino.toString(), hash);
		} catch (InfrastructureException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new InfrastructureException("FACTUCORE.XSD.IMPORTACION.ERROR", exception);
		}
	}
}
