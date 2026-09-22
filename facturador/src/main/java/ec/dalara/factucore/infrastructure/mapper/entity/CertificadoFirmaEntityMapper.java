package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.certificadofirma.CertificadoFirmaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CertificadoFirmaEntityMapper {
    default CertificadoFirmaModel toModel(CertificadoFirma r){if(r==null)return null;return new CertificadoFirmaModel(r.getEmpresa().getId(),r.getNombreArchivo(),r.getRutaCertificado(),r.getFechaInicio(),r.getFechaFin());}
    CertificadoFirma toEntity(CertificadoFirmaModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.Empresa empresa(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.Empresa();x.setId(id);return x;}
}
