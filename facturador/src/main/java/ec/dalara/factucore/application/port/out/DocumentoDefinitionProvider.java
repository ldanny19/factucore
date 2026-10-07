package ec.dalara.factucore.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

public interface DocumentoDefinitionProvider {

	Optional<VersionDocumentoXsdModel> obtenerVersion(String codigoDocumento, String versionXsd,
			LocalDateTime fechaEmision);

	Optional<DocumentDefinitionModel> obtenerDefinicion(String codigoDocumento, String versionXsd,
			LocalDateTime fechaEmision);
}
