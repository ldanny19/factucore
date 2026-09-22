package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.EnumeracionXsdRequest;
import ec.dalara.factucore.application.contract.response.EnumeracionXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;

@Mapper(config = MapStructConfig.class)
public interface EnumeracionXsdMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "elementoXsd", source = "idElementoXsd")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	EnumeracionXsd toEntity(EnumeracionXsdRequest request);

	@Mapping(target = "idElementoXsd", source = "elementoXsd.id")
	EnumeracionXsdResponse toResponse(EnumeracionXsd entity);

	default ElementoXsd mapElementoXsd(Long idElementoXsd) {

		if (idElementoXsd == null) {
			return null;
		}

		ElementoXsd elemento = new ElementoXsd();
		elemento.setId(idElementoXsd);

		return elemento;
	}
}