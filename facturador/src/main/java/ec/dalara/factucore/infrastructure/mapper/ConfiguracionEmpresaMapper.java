package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ConfiguracionEmpresaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", source = "idEmpresa")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    ConfiguracionEmpresa toEntity(ConfiguracionEmpresaRequest request);

    @Mapping(target = "idEmpresa", source = "empresa.id")
    ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresa entity);

    default Empresa mapEmpresa(Long idEmpresa) {
        if (idEmpresa == null) {
            return null;
        }

        Empresa empresa = new Empresa();
        empresa.setId(idEmpresa);
        return empresa;
    }
}