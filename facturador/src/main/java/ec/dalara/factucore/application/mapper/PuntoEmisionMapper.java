package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.domain.puntoemision.PuntoEmisionModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface PuntoEmisionMapper {
    PuntoEmisionModel toModel(PuntoEmisionRequest request);
    PuntoEmisionResponse toResponse(PuntoEmisionModel model);
}