package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteInformacionAdicionalModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteInformacionAdicionalEntityMapper {
 ComprobanteInformacionAdicionalModel toModel(ComprobanteInformacionAdicional entity);
 @Mapping(target="id",ignore=true)
 ComprobanteInformacionAdicional toEntity(ComprobanteInformacionAdicionalModel model);
}