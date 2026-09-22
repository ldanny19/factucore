package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteRetencionModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteRetencionEntityMapper {
 ComprobanteRetencionModel toModel(ComprobanteRetencion entity);
 @Mapping(target="id",ignore=true)
 ComprobanteRetencion toEntity(ComprobanteRetencionModel model);
}