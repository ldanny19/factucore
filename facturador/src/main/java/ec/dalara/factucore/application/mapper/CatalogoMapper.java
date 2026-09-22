package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.CatalogoRequest;
import ec.dalara.factucore.application.contract.response.CatalogoResponse;
import ec.dalara.factucore.domain.catalogo.CatalogoModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface CatalogoMapper {
    CatalogoModel toModel(CatalogoRequest request);
    CatalogoResponse toResponse(CatalogoModel model);
}