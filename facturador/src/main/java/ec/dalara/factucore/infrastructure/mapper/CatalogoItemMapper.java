package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CatalogoItemRequest;
import ec.dalara.factucore.application.contract.response.CatalogoItemResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CatalogoItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "catalogo", source = "idCatalogo")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    CatalogoItem toEntity(CatalogoItemRequest request);

    @Mapping(target = "idCatalogo", source = "catalogo.id")
    CatalogoItemResponse toResponse(CatalogoItem entity);

    default Catalogo mapCatalogo(Long idCatalogo) {
        if (idCatalogo == null) {
            return null;
        }

        Catalogo catalogo = new Catalogo();
        catalogo.setId(idCatalogo);
        return catalogo;
    }
}