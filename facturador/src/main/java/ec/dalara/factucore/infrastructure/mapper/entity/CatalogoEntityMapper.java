package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.catalogo.CatalogoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CatalogoEntityMapper {
    default CatalogoModel toModel(Catalogo r){if(r==null)return null;return new CatalogoModel(r.getCodigo(),r.getNombre(),r.getDescripcion());}
    Catalogo toEntity(CatalogoModel model);
}
