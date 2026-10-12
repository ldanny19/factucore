package ec.dalara.factucore.application.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

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

	public XsdImportResult importar(InputStream inputStream, String rutaXsd, XsdImportRequest request) {
		if (inputStream == null) {
			throw new InfrastructureException("FACTUCORE.XSD.ARCHIVO.REQUERIDO");
		}
		if (rutaXsd == null || rutaXsd.isBlank()) {
			throw new InfrastructureException("FACTUCORE.XSD.RUTA.REQUERIDA");
		}

		try {
			byte[] contenido = inputStream.readAllBytes();
			XsdDefinitionSource definition = parser.parse(new ByteArrayInputStream(contenido), rutaXsd);
			String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
			return persistence.persist(request, definition, rutaXsd, hash);
		} catch (InfrastructureException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new InfrastructureException("FACTUCORE.XSD.IMPORTACION.ERROR", exception);
		}
	}
}
