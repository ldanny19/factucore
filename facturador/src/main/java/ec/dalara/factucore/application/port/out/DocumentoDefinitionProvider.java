package ec.dalara.factucore.application.port.out;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DocumentoDefinitionProvider {

    Optional<VersionDocumentoXsdModel> obtenerVersionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    );

    Optional<DocumentDefinitionModel> obtenerDefinicionVigente(
            String codigoDocumento,
            LocalDateTime fechaEmision
    );
}