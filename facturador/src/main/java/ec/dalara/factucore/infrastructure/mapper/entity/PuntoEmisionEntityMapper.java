package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.puntoemision.PuntoEmisionModel;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface PuntoEmisionEntityMapper {
 default PuntoEmisionModel toModel(PuntoEmision e){if(e==null)return null;return new PuntoEmisionModel(e.getId(),e.getEstablecimiento().getId(),e.getCodigo(),e.getNombre());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="establecimiento", ignore=true)
 PuntoEmision toEntity(PuntoEmisionModel model);
}