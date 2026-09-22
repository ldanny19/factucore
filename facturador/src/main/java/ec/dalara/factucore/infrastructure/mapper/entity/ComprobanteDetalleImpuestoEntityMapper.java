package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleImpuestoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteDetalleImpuestoEntityMapper {
 ComprobanteDetalleImpuestoModel toModel(ComprobanteDetalleImpuesto entity);
 @Mapping(target="id",ignore=true)
 ComprobanteDetalleImpuesto toEntity(ComprobanteDetalleImpuestoModel model);
}