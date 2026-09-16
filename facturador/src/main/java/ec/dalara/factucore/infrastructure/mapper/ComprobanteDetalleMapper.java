package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteDetalleRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteDetalleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comprobante", source = "idComprobante")
    @Mapping(target = "numeroLinea", source = "orden")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    ComprobanteDetalle toEntity(ComprobanteDetalleRequest request);

    @Mapping(target = "idComprobante", source = "comprobante.id")
    @Mapping(target = "orden", source = "numeroLinea")
    ComprobanteDetalleResponse toResponse(ComprobanteDetalle entity);

    default Comprobante mapComprobante(Long idComprobante) {

        if (idComprobante == null) {
            return null;
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setId(idComprobante);

        return comprobante;
    }
}