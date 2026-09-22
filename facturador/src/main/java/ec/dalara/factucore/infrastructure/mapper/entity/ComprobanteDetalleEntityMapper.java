package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteDetalleEntityMapper {
 ComprobanteDetalleModel toModel(ComprobanteDetalle entity);
 @Mapping(target="id",ignore=true)
 ComprobanteDetalle toEntity(ComprobanteDetalleModel model);
}