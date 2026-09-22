package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.ComprobanteRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteModel;
import ec.dalara.factucore.domain.comprobante.NumeroComprobante;
@Mapper(config=MapStructApplicationConfig.class)
public interface ComprobanteMapper {
 default ComprobanteModel toModel(ComprobanteRequest r){if(r==null)return null;return new ComprobanteModel(null,r.getIdEmpresa(),r.getIdEstablecimiento(),r.getIdPuntoEmision(),r.getCodigoDocumento(),new NumeroComprobante(r.getSecuencial()),r.getFechaEmision());}
 default ComprobanteResponse toResponse(ComprobanteModel m){if(m==null)return null;return ComprobanteResponse.builder().id(m.getId()).idEmpresa(m.getEmpresaId()).idEstablecimiento(m.getEstablecimientoId()).idPuntoEmision(m.getPuntoEmisionId()).codigoDocumento(m.getCodigoDocumento()).secuencial(m.getNumeroComprobante().valor()).estadoProceso(m.getEstadoProceso()).claveAcceso(m.getClaveAcceso()==null?null:m.getClaveAcceso().valor()).fechaEmision(m.getFechaEmision()).build();}
}