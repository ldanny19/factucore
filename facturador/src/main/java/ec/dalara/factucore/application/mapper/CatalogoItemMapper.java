package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.CatalogoItemRequest;
import ec.dalara.factucore.application.contract.response.CatalogoItemResponse;
import ec.dalara.factucore.domain.catalogo.CatalogoItemModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface CatalogoItemMapper {
    CatalogoItemModel toModel(CatalogoItemRequest request);
    CatalogoItemResponse toResponse(CatalogoItemModel model);
}