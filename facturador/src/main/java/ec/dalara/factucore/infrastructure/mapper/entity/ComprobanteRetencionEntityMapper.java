package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteRetencionModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteRetencionEntityMapper {
 default ComprobanteRetencionModel toModel(ComprobanteRetencion e){if(e==null)return null;return new ComprobanteRetencionModel(e.getCodigoImpuesto(),e.getCodigoRetencion(),e.getPorcentajeRetener(),e.getBaseImponible(),e.getValorRetenido(),e.getNumeroDocumentoSustento(),e.getFechaEmisionDocumentoSustento());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobante", ignore=true)
 ComprobanteRetencion toEntity(ComprobanteRetencionModel model);
}