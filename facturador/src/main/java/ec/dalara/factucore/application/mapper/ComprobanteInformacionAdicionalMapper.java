package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteInformacionAdicionalRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteInformacionAdicionalResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteInformacionAdicionalModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteInformacionAdicionalMapper {
 default ComprobanteInformacionAdicionalModel toModel(ComprobanteInformacionAdicionalRequest r){if(r==null)return null;return new ComprobanteInformacionAdicionalModel(r.getNombre(),r.getValor());}
 ComprobanteInformacionAdicionalResponse toResponse(ComprobanteInformacionAdicionalModel model);
}