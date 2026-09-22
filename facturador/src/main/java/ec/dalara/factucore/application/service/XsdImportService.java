package ec.dalara.factucore.application.service;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.application.port.out.XsdDefinitionPersistencePort;
import ec.dalara.factucore.application.port.out.XsdParserPort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class XsdImportService {

    private final XsdParserPort parser;
    private final XsdDefinitionPersistencePort persistence;

    public XsdImportResult importar(InputStream inputStream, String systemId, XsdImportRequest request) {
        XsdDefinitionSource definition = parser.parse(inputStream, systemId);
        return persistence.persist(request, definition);
    }
}
