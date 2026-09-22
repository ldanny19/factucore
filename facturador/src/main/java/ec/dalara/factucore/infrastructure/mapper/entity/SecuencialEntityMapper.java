package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.secuencial.SecuencialModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface SecuencialEntityMapper {
    default SecuencialModel toModel(Secuencial r){if(r==null)return null;return new SecuencialModel(r.getId(),r.getPuntoEmision().getId(),r.getCodigoDocumento(),r.getUltimoSecuencial());}
    Secuencial toEntity(SecuencialModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision puntoEmision(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision();x.setId(id);return x;}
}
