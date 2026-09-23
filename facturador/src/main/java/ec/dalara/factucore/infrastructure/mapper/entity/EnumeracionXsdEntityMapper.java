package ec.dalara.factucore.infrastructure.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;

@Mapper(config = ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EnumeracionXsdEntityMapper {
    default EnumeracionXsdModel toModel(EnumeracionXsd e) {
        if (e == null) return null;
        return new EnumeracionXsdModel(e.getId(), e.getElementoXsd().getId(), e.getValor(), e.getDescripcion(), e.getOrden());
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "elementoXsd", ignore = true)
    EnumeracionXsd toEntity(EnumeracionXsdModel model);
}