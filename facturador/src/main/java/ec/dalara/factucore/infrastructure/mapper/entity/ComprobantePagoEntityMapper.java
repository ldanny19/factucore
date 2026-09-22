package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobantePagoModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobantePagoEntityMapper {
 ComprobantePagoModel toModel(ComprobantePago entity);
 @Mapping(target="id",ignore=true)
 ComprobantePago toEntity(ComprobantePagoModel model);
}