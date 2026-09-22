package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.ElementoXsdRequest;
import ec.dalara.factucore.application.contract.response.ElementoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;

@Mapper(config = MapStructConfig.class)
public interface ElementoXsdMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "versionDocumentoXsd", source = "idVersionDocumentoXsd")
	@Mapping(target = "elementoPadre", source = "idElementoPadre")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	ElementoXsd toEntity(ElementoXsdRequest request);

	@Mapping(target = "idVersionDocumentoXsd", source = "versionDocumentoXsd.id")
	@Mapping(target = "idElementoPadre", source = "elementoPadre.id")
	ElementoXsdResponse toResponse(ElementoXsd entity);

	default VersionDocumentoXsd mapVersionDocumentoXsd(Long idVersionDocumentoXsd) {

		if (idVersionDocumentoXsd == null) {
			return null;
		}

		VersionDocumentoXsd version = new VersionDocumentoXsd();
		version.setId(idVersionDocumentoXsd);

		return version;
	}

	default ElementoXsd mapElementoPadre(Long idElementoPadre) {

		if (idElementoPadre == null) {
			return null;
		}

		ElementoXsd elemento = new ElementoXsd();
		elemento.setId(idElementoPadre);

		return elemento;
	}
}