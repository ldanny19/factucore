package ec.dalara.factucore.application.mapper;

import org.mapstruct.Mapper;

import ec.dalara.factucore.application.contract.response.ComprobanteResponse;
import ec.dalara.factucore.domain.comprobante.ComprobanteModel;

@Mapper(config = MapStructApplicationConfig.class)
public interface ComprobanteMapper {

    default ComprobanteResponse toResponse(ComprobanteModel model) {
        if (model == null) {
            return null;
        }

        return ComprobanteResponse.builder()
                .id(model.getId())
                .idEmpresa(model.getEmpresaId())
                .idEstablecimiento(model.getEstablecimientoId())
                .idPuntoEmision(model.getPuntoEmisionId())
                .codigoDocumento(model.getCodigoDocumento())
                .secuencial(model.getNumeroComprobante().valor())
                .estadoProceso(model.getEstadoProceso())
                .claveAcceso(model.getClaveAcceso() == null ? null : model.getClaveAcceso().valor())
                .fechaEmision(model.getFechaEmision())
                .build();
    }
}
