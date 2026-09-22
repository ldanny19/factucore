package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;

@Mapper(config = MapStructConfig.class)
public interface EmpresaMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	Empresa toEntity(EmpresaRequest request);

	EmpresaResponse toResponse(Empresa entity);
}