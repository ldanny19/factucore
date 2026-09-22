package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.documentoxsd.importacion.XsdDefinitionSource;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;

public interface XsdDefinitionPersistencePort {

    XsdImportResult persist(XsdImportRequest request, XsdDefinitionSource definition);
}
