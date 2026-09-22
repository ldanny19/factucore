package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ElementoXsdEntityMapper {
 ElementoXsdModel toModel(ElementoXsd entity);
 @Mapping(target="id",ignore=true)
 ElementoXsd toEntity(ElementoXsdModel model);
}