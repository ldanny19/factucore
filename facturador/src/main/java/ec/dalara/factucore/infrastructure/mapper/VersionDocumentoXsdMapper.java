package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.VersionDocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.VersionDocumentoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface VersionDocumentoXsdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentoXsd", source = "idDocumentoXsd")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    VersionDocumentoXsd toEntity(
            VersionDocumentoXsdRequest request
    );

    @Mapping(
            target = "idDocumentoXsd",
            source = "documentoXsd.id"
    )
    VersionDocumentoXsdResponse toResponse(
            VersionDocumentoXsd entity
    );

    default VersionDocumentoXsdModel toModel(
            VersionDocumentoXsd entity
    ) {
        if (entity == null) {
            return null;
        }

        return new VersionDocumentoXsdModel(
                entity.getId(),
                entity.getDocumentoXsd().getId(),
                entity.getVersion(),
                entity.getVersionXsd(),
                entity.getFechaInicio(),
                entity.getFechaFin()
        );
    }

    default DocumentoXsd mapDocumentoXsd(
            Long idDocumentoXsd
    ) {
        if (idDocumentoXsd == null) {
            return null;
        }

        DocumentoXsd documentoXsd = new DocumentoXsd();
        documentoXsd.setId(idDocumentoXsd);

        return documentoXsd;
    }
}