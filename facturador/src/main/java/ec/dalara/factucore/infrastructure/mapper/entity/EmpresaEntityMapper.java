package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.empresa.EmpresaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EmpresaEntityMapper {
 default EmpresaModel toModel(Empresa e){if(e==null)return null;return new EmpresaModel(e.getId(),e.getRuc(),e.getRazonSocial(),e.getNombreComercial(),e.getDireccionMatriz(),e.getObligadoContabilidad(),e.getContribuyenteRimpe());}
 @Mapping(target="id",ignore=true)
 
 Empresa toEntity(EmpresaModel model);
}