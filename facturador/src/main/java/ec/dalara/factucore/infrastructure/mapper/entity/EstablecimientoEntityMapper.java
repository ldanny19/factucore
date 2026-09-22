package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.establecimiento.EstablecimientoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EstablecimientoEntityMapper {
 default EstablecimientoModel toModel(Establecimiento e){if(e==null)return null;return new EstablecimientoModel(e.getId(),e.getEmpresa().getId(),e.getCodigo(),e.getNombre(),e.getDireccion());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="empresa", ignore=true)
 Establecimiento toEntity(EstablecimientoModel model);
}