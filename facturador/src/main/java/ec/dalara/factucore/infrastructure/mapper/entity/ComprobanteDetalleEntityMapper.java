package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteDetalleEntityMapper {
 default ComprobanteDetalleModel toModel(ComprobanteDetalle e){if(e==null)return null;return new ComprobanteDetalleModel(e.getOrden(),e.getCodigoPrincipal(),e.getCodigoAuxiliar(),e.getDescripcion(),e.getCantidad(),e.getPrecioUnitario(),e.getDescuento(),e.getPrecioTotalSinImpuesto());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobante", ignore=true)
 ComprobanteDetalle toEntity(ComprobanteDetalleModel model);
}