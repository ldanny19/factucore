package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface MapeoXsdEntityMapper {
 default MapeoXsdModel toModel(MapeoXsd e){if(e==null)return null;return new MapeoXsdModel(e.getId(),e.getVersionDocumentoXsd().getId(),e.getRutaOrigen(),e.getElementoXsd()==null?null:e.getElementoXsd().getId(),e.getAtributoXsd()==null?null:e.getAtributoXsd().getId(),e.getTipoMapeo());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="versionDocumentoXsd", ignore=true)
    @Mapping(target="elementoXsd", ignore=true)
    @Mapping(target="atributoXsd", ignore=true)
 MapeoXsd toEntity(MapeoXsdModel model);
}