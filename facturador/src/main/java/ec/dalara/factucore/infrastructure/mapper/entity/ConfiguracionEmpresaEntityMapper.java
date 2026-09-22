package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.configuracionempresa.ConfiguracionEmpresaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ConfiguracionEmpresaEntityMapper {
 default ConfiguracionEmpresaModel toModel(ConfiguracionEmpresa e){if(e==null)return null;return new ConfiguracionEmpresaModel(e.getEmpresa().getId(),e.getClave(),e.getValor(),e.getTipoDato(),e.getFechaVigenciaDesde(),e.getFechaVigenciaHasta());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="empresa", ignore=true)
 ConfiguracionEmpresa toEntity(ConfiguracionEmpresaModel model);
}