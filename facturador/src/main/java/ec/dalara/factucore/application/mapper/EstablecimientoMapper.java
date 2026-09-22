package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.domain.establecimiento.EstablecimientoModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface EstablecimientoMapper {
    EstablecimientoModel toModel(EstablecimientoRequest request);
    EstablecimientoResponse toResponse(EstablecimientoModel model);
}