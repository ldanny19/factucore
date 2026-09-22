package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.ComprobanteInformacionAdicionalRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteInformacionAdicionalResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteInformacionAdicionalMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "comprobante", source = "idComprobante")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	ComprobanteInformacionAdicional toEntity(ComprobanteInformacionAdicionalRequest request);

	@Mapping(target = "idComprobante", source = "comprobante.id")
	ComprobanteInformacionAdicionalResponse toResponse(ComprobanteInformacionAdicional entity);

	default Comprobante mapComprobante(Long idComprobante) {

		if (idComprobante == null) {
			return null;
		}

		Comprobante comprobante = new Comprobante();
		comprobante.setId(idComprobante);

		return comprobante;
	}
}