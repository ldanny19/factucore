package ec.dalara.factucore.application.port.out;

import java.util.Optional;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

public interface DocumentoDefinitionProvider {

	Optional<VersionDocumentoXsdModel> obtenerVersion(Long idDocumentoXsd, String versionXsd);

	Optional<DocumentDefinitionModel> obtenerDefinicion(Long idDocumentoXsd, String versionXsd);
}
