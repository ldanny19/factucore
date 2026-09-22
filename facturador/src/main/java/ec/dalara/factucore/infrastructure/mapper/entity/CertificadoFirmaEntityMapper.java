package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.certificadofirma.CertificadoFirmaModel;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface CertificadoFirmaEntityMapper {
 default CertificadoFirmaModel toModel(CertificadoFirma e){if(e==null)return null;return new CertificadoFirmaModel(e.getId(), e.getEmpresa().getId(), e.getNombreArchivo(), e.getRutaCertificado(), e.getFechaInicio(), e.getFechaFin());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="empresa", ignore=true)
 CertificadoFirma toEntity(CertificadoFirmaModel model);
}