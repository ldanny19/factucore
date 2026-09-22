package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteAuditoriaRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteAuditoriaResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteAuditoriaModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteAuditoriaMapper {
 default ComprobanteAuditoriaModel toModel(ComprobanteAuditoriaRequest r){if(r==null)return null;return new ComprobanteAuditoriaModel(r.getEstadoAnterior(),r.getEstadoNuevo(),r.getCodigoError(),r.getMensajeError(),null);}
 ComprobanteAuditoriaResponse toResponse(ComprobanteAuditoriaModel model);
}