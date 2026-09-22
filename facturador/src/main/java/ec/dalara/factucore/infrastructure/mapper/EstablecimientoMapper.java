package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;

@Mapper(config = MapStructConfig.class)
public interface EstablecimientoMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "empresa", source = "idEmpresa")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	Establecimiento toEntity(EstablecimientoRequest request);

	@Mapping(target = "idEmpresa", source = "empresa.id")
	EstablecimientoResponse toResponse(Establecimiento entity);

	default Empresa mapEmpresa(Long idEmpresa) {
		if (idEmpresa == null) {
			return null;
		}

		Empresa empresa = new Empresa();
		empresa.setId(idEmpresa);
		return empresa;
	}
}