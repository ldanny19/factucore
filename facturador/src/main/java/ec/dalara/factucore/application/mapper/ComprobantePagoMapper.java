package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobantePagoRequest;
import ec.dalara.factucore.application.contract.response.ComprobantePagoResponse;
import ec.dalara.factucore.domain.comprobante.ComprobantePagoModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobantePagoMapper {
 default ComprobantePagoModel toModel(ComprobantePagoRequest r){if(r==null)return null;return new ComprobantePagoModel(r.getCodigoFormaPago(),r.getTotal(),r.getPlazo(),r.getUnidadTiempo());}
 ComprobantePagoResponse toResponse(ComprobantePagoModel model);
}