package ec.dalara.factucore.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

public interface DocumentoDefinitionProvider {

	Optional<VersionDocumentoXsdModel> obtenerVersionVigente(String codigoDocumento, LocalDateTime fechaEmision);

	Optional<DocumentDefinitionModel> obtenerDefinicionVigente(String codigoDocumento, LocalDateTime fechaEmision);
}