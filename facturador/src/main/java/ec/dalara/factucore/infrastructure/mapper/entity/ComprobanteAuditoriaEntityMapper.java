package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteAuditoriaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteAuditoriaEntityMapper {
 default ComprobanteAuditoriaModel toModel(ComprobanteAuditoria e){if(e==null)return null;return new ComprobanteAuditoriaModel(e.getEstadoAnterior(),e.getEstadoNuevo(),e.getCodigoError(),e.getMensajeError(),e.getFecha());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="comprobante", ignore=true)
 ComprobanteAuditoria toEntity(ComprobanteAuditoriaModel model);
}