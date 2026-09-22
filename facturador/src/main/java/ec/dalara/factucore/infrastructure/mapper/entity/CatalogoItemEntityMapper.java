package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.catalogo.CatalogoItemModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CatalogoItemEntityMapper {
 default CatalogoItemModel toModel(CatalogoItem e){if(e==null)return null;return new CatalogoItemModel(e.getCatalogo().getId(),e.getCodigo(),e.getNombre(),e.getDescripcion(),e.getOrden());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="catalogo", ignore=true)
 CatalogoItem toEntity(CatalogoItemModel model);
}