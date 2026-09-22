package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.catalogo.CatalogoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CatalogoEntityMapper {
 default CatalogoModel toModel(Catalogo e){if(e==null)return null;return new CatalogoModel(e.getCodigo(),e.getNombre(),e.getDescripcion());}
 @Mapping(target="id",ignore=true)
 
 Catalogo toEntity(CatalogoModel model);
}