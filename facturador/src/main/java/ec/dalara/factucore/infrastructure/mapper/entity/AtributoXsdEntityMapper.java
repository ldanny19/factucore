package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface AtributoXsdEntityMapper {
 default AtributoXsdModel toModel(AtributoXsd e){if(e==null)return null;return new AtributoXsdModel(e.getId(),e.getElementoXsd().getId(),e.getNombre(),e.getTipoDato(),e.getObligatorio(),e.getValorPredeterminado(),e.getPatron());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="elementoXsd", ignore=true)
 AtributoXsd toEntity(AtributoXsdModel model);
}