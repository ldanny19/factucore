package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteAuditoriaRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteAuditoriaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteAuditoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comprobante", source = "idComprobante")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    ComprobanteAuditoria toEntity(ComprobanteAuditoriaRequest request);

    @Mapping(target = "idComprobante", source = "comprobante.id")
    ComprobanteAuditoriaResponse toResponse(ComprobanteAuditoria entity);

    default Comprobante mapComprobante(Long idComprobante) {

        if (idComprobante == null) {
            return null;
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setId(idComprobante);

        return comprobante;
    }
}