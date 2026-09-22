package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.catalogo.CatalogoItemModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CatalogoItemEntityMapper {
    default CatalogoItemModel toModel(CatalogoItem r){if(r==null)return null;return new CatalogoItemModel(r.getCatalogo().getId(),r.getCodigo(),r.getNombre(),r.getDescripcion(),r.getOrden());}
    CatalogoItem toEntity(CatalogoItemModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.Catalogo catalogo(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.Catalogo();x.setId(id);return x;}
}
