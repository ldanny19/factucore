package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;

@Mapper(config = MapStructConfig.class)
public interface SecuencialMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "puntoEmision", source = "idPuntoEmision")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	Secuencial toEntity(SecuencialRequest request);

	@Mapping(target = "idPuntoEmision", source = "puntoEmision.id")
	SecuencialResponse toResponse(Secuencial entity);

	default PuntoEmision mapPuntoEmision(Long idPuntoEmision) {
		if (idPuntoEmision == null) {
			return null;
		}

		PuntoEmision puntoEmision = new PuntoEmision();
		puntoEmision.setId(idPuntoEmision);
		return puntoEmision;
	}
}