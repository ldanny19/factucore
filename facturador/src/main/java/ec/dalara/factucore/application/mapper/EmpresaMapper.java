package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.domain.empresa.EmpresaModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface EmpresaMapper {
    EmpresaModel toModel(EmpresaRequest request);
    EmpresaResponse toResponse(EmpresaModel model);
}