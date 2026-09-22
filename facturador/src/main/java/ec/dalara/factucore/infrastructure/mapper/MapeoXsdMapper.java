package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.MapeoXsdRequest;
import ec.dalara.factucore.application.contract.response.MapeoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface MapeoXsdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionDocumentoXsd", ignore = true)
    @Mapping(target = "elementoXsd", ignore = true)
    @Mapping(target = "atributoXsd", ignore = true)
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    MapeoXsd toEntity(MapeoXsdRequest request);

    @Mapping(target = "idVersionDocumentoXsd", source = "versionDocumentoXsd.id")
    @Mapping(target = "idElementoXsd", source = "elementoXsd.id")
    @Mapping(target = "idAtributoXsd", source = "atributoXsd.id")
    MapeoXsdResponse toResponse(MapeoXsd entity);

    default MapeoXsdModel toModel(MapeoXsd entity) {
        if (entity == null) {
            return null;
        }

        return new MapeoXsdModel(
                entity.getId(),
                entity.getVersionDocumentoXsd().getId(),
                entity.getRutaOrigen(),
                entity.getElementoXsd() == null
                        ? null
                        : entity.getElementoXsd().getId(),
                entity.getAtributoXsd() == null
                        ? null
                        : entity.getAtributoXsd().getId(),
                entity.getTipoMapeo()
        );
    }
}