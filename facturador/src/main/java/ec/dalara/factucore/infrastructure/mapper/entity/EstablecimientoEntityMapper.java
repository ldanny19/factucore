package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.establecimiento.EstablecimientoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EstablecimientoEntityMapper {
    default EstablecimientoModel toModel(Establecimiento r){if(r==null)return null;return new EstablecimientoModel(r.getId(),r.getEmpresa().getId(),r.getCodigo(),r.getNombre(),r.getDireccion());}
    Establecimiento toEntity(EstablecimientoModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.Empresa empresa(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.Empresa();x.setId(id);return x;}
}
