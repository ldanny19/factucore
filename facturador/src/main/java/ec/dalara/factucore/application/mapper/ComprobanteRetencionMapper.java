package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteRetencionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteRetencionResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteRetencionModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteRetencionMapper {
 default ComprobanteRetencionModel toModel(ComprobanteRetencionRequest r){if(r==null)return null;return new ComprobanteRetencionModel(r.getCodigoImpuesto(),r.getCodigoRetencion(),r.getPorcentajeRetener(),r.getBaseImponible(),r.getValorRetenido(),r.getNumeroDocumentoSustento(),r.getFechaEmisionDocumentoSustento());}
 ComprobanteRetencionResponse toResponse(ComprobanteRetencionModel model);
}