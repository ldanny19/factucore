package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.ComprobanteRetencionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteRetencionResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteRetencionMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "comprobante", source = "idComprobante")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	ComprobanteRetencion toEntity(ComprobanteRetencionRequest request);

	@Mapping(target = "idComprobante", source = "comprobante.id")
	ComprobanteRetencionResponse toResponse(ComprobanteRetencion entity);

	default Comprobante mapComprobante(Long idComprobante) {

		if (idComprobante == null) {
			return null;
		}

		Comprobante comprobante = new Comprobante();
		comprobante.setId(idComprobante);

		return comprobante;
	}
}