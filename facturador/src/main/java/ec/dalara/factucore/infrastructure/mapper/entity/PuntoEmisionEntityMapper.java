package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.puntoemision.PuntoEmisionModel;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface PuntoEmisionEntityMapper {
    default PuntoEmisionModel toModel(PuntoEmision r){if(r==null)return null;return new PuntoEmisionModel(r.getId(),r.getEstablecimiento().getId(),r.getCodigo(),r.getNombre());}
    PuntoEmision toEntity(PuntoEmisionModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento establecimiento(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento();x.setId(id);return x;}
}
