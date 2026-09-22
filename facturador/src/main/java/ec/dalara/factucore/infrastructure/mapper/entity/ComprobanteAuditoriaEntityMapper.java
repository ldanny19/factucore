package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteAuditoriaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteAuditoriaEntityMapper {
 ComprobanteAuditoriaModel toModel(ComprobanteAuditoria entity);
 @Mapping(target="id",ignore=true)
 ComprobanteAuditoria toEntity(ComprobanteAuditoriaModel model);
}