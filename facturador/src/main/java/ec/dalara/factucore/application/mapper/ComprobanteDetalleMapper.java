package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteDetalleRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteDetalleModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteDetalleMapper {
 default ComprobanteDetalleModel toModel(ComprobanteDetalleRequest r){if(r==null)return null;return new ComprobanteDetalleModel(r.getOrden(),r.getCodigoPrincipal(),r.getCodigoAuxiliar(),r.getDescripcion(),r.getCantidad(),r.getPrecioUnitario(),r.getDescuento(),r.getPrecioTotalSinImpuesto());}
 ComprobanteDetalleResponse toResponse(ComprobanteDetalleModel model);
}