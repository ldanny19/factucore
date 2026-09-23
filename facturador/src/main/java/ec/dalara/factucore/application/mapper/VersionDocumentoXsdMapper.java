package ec.dalara.factucore.application.mapper;

import org.mapstruct.Mapper;

import ec.dalara.factucore.application.contract.request.VersionDocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.VersionDocumentoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;

@Mapper(config = MapStructApplicationConfig.class)
public interface VersionDocumentoXsdMapper {

    default VersionDocumentoXsdModel toModel(VersionDocumentoXsdRequest request) {
        if (request == null) {
            return null;
        }

        return new VersionDocumentoXsdModel(
                null,
                request.getIdDocumentoXsd(),
                request.getVersion(),
                request.getNombreArchivo(),
                request.getNamespaceXml(),
                request.getElementoRaiz(),
                request.getPlantillaJson(),
                request.getEsquemaJson(),
                request.getFechaInicio(),
                request.getFechaFin());
    }

    VersionDocumentoXsdResponse toResponse(VersionDocumentoXsdModel model);
}
