package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.domain.configuracionempresa.ConfiguracionEmpresaModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface ConfiguracionEmpresaMapper {
    ConfiguracionEmpresaModel toModel(ConfiguracionEmpresaRequest request);
    ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresaModel model);
}