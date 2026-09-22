package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobantePagoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobantePagoEntityMapper {
 default ComprobantePagoModel toModel(ComprobantePago e){if(e==null)return null;return new ComprobantePagoModel(e.getCodigoFormaPago(),e.getTotal(),e.getPlazo(),e.getUnidadTiempo());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobante", ignore=true)
 ComprobantePago toEntity(ComprobantePagoModel model);
}