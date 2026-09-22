package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.secuencial.SecuencialModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface SecuencialEntityMapper {
 default SecuencialModel toModel(Secuencial e){if(e==null)return null;return new SecuencialModel(e.getId(),e.getPuntoEmision().getId(),e.getCodigoDocumento(),e.getUltimoSecuencial());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="puntoEmision", ignore=true)
 Secuencial toEntity(SecuencialModel model);
}