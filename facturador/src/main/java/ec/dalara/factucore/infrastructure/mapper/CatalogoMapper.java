package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CatalogoRequest;
import ec.dalara.factucore.application.contract.response.CatalogoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CatalogoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    Catalogo toEntity(CatalogoRequest request);

    CatalogoResponse toResponse(Catalogo entity);
}