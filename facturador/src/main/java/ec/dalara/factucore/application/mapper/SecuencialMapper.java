package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.domain.secuencial.SecuencialModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface SecuencialMapper {
    SecuencialModel toModel(SecuencialRequest request);
    SecuencialResponse toResponse(SecuencialModel model);
}