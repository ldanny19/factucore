package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.ComprobanteDetalleImpuestoRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleImpuestoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteDetalleImpuestoMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "comprobanteDetalle", source = "idComprobanteDetalle")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	ComprobanteDetalleImpuesto toEntity(ComprobanteDetalleImpuestoRequest request);

	@Mapping(target = "idComprobanteDetalle", source = "comprobanteDetalle.id")
	ComprobanteDetalleImpuestoResponse toResponse(ComprobanteDetalleImpuesto entity);

	default ComprobanteDetalle mapComprobanteDetalle(Long idComprobanteDetalle) {

		if (idComprobanteDetalle == null) {
			return null;
		}

		ComprobanteDetalle detalle = new ComprobanteDetalle();
		detalle.setId(idComprobanteDetalle);

		return detalle;
	}
}