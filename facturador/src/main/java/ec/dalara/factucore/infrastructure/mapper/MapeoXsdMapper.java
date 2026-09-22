package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface MapeoXsdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "versionDocumentoXsd",
            source = "versionDocumentoXsdId"
    )
    @Mapping(target = "elementoXsd", ignore = true)
    @Mapping(target = "atributoXsd", ignore = true)
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    MapeoXsd toEntity(
            Long versionDocumentoXsdId,
            String rutaOrigen,
            Long elementoXsdId,
            Long atributoXsdId,
            String tipoMapeo
    );

    default VersionDocumentoXsd mapVersionDocumentoXsd(
            Long id
    ) {
        if (id == null) {
            return null;
        }

        VersionDocumentoXsd entity = new VersionDocumentoXsd();
        entity.setId(id);
        return entity;
    }

    default ElementoXsd mapElementoXsd(Long id) {
        if (id == null) {
            return null;
        }

        ElementoXsd entity = new ElementoXsd();
        entity.setId(id);
        return entity;
    }

    default AtributoXsd mapAtributoXsd(Long id) {
        if (id == null) {
            return null;
        }

        AtributoXsd entity = new AtributoXsd();
        entity.setId(id);
        return entity;
    }

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