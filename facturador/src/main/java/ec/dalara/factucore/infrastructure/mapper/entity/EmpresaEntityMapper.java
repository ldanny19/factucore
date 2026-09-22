package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.empresa.EmpresaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface EmpresaEntityMapper {
    default EmpresaModel toModel(Empresa r){if(r==null)return null;return new EmpresaModel(r.getId(),r.getRuc(),r.getRazonSocial(),r.getNombreComercial(),r.getDireccionMatriz(),r.getObligadoContabilidad(),r.getContribuyenteRimpe());}
    Empresa toEntity(EmpresaModel model);
}
