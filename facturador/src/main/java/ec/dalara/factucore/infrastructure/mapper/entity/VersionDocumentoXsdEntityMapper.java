package ec.dalara.factucore.infrastructure.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;

@Mapper(config = ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface VersionDocumentoXsdEntityMapper {

    default VersionDocumentoXsdModel toModel(VersionDocumentoXsd e) {
        if (e == null) {
            return null;
        }

        return new VersionDocumentoXsdModel(
                e.getId(),
                e.getDocumentoXsd().getId(),
                e.getVersion(),
                e.getNombreArchivo(),
                e.getNamespaceXml(),
                e.getElementoRaiz(),
                e.getPlantillaJson(),
                e.getEsquemaJson(),
                e.getFechaInicio(),
                e.getFechaFin());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentoXsd", ignore = true)
    VersionDocumentoXsd toEntity(VersionDocumentoXsdModel model);
}
