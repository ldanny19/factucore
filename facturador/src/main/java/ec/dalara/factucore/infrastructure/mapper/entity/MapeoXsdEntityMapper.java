package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface MapeoXsdEntityMapper {
 MapeoXsdModel toModel(MapeoXsd entity);
 @Mapping(target="id",ignore=true)
 MapeoXsd toEntity(MapeoXsdModel model);
}