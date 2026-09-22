package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EnumeracionXsdEntityMapper {
 EnumeracionXsdModel toModel(EnumeracionXsd entity);
 @Mapping(target="id",ignore=true)
 EnumeracionXsd toEntity(EnumeracionXsdModel model);
}