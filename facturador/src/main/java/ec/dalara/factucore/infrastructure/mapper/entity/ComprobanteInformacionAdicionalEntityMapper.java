package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteInformacionAdicionalModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteInformacionAdicionalEntityMapper {
 default ComprobanteInformacionAdicionalModel toModel(ComprobanteInformacionAdicional e){if(e==null)return null;return new ComprobanteInformacionAdicionalModel(e.getNombre(),e.getValor());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobante", ignore=true)
 ComprobanteInformacionAdicional toEntity(ComprobanteInformacionAdicionalModel model);
}