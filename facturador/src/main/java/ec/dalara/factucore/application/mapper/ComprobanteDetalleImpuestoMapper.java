package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteDetalleImpuestoRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleImpuestoResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleImpuestoModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteDetalleImpuestoMapper {
 default ComprobanteDetalleImpuestoModel toModel(ComprobanteDetalleImpuestoRequest r){if(r==null)return null;return new ComprobanteDetalleImpuestoModel(r.getCodigoImpuesto(),r.getCodigoPorcentaje(),r.getTarifa(),r.getBaseImponible(),r.getValor(),r.getValorDevolucionIva());}
 ComprobanteDetalleImpuestoResponse toResponse(ComprobanteDetalleImpuestoModel model);
}