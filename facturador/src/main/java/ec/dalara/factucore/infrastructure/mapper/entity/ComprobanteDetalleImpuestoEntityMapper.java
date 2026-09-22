package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleImpuestoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteDetalleImpuestoEntityMapper {
 default ComprobanteDetalleImpuestoModel toModel(ComprobanteDetalleImpuesto e){if(e==null)return null;return new ComprobanteDetalleImpuestoModel(e.getCodigoImpuesto(),e.getCodigoPorcentaje(),e.getTarifa(),e.getBaseImponible(),e.getValor(),e.getValorDevolucionIva());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobanteDetalle", ignore=true)
 ComprobanteDetalleImpuesto toEntity(ComprobanteDetalleImpuestoModel model);
}