package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.configuracionempresa.ConfiguracionEmpresaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ConfiguracionEmpresaEntityMapper {
    default ConfiguracionEmpresaModel toModel(ConfiguracionEmpresa r){if(r==null)return null;return new ConfiguracionEmpresaModel(r.getEmpresa().getId(),r.getClave(),r.getValor(),r.getTipoDato(),r.getFechaVigenciaDesde(),r.getFechaVigenciaHasta());}
    ConfiguracionEmpresa toEntity(ConfiguracionEmpresaModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.Empresa empresa(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.Empresa();x.setId(id);return x;}
}
