package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface VersionDocumentoXsdEntityMapper {
    default VersionDocumentoXsdModel toModel(VersionDocumentoXsd r){if(r==null)return null;return new VersionDocumentoXsdModel(r.getId(),r.getDocumentoXsd().getId(),r.getVersion(),r.getNamespaceXml(),r.getElementoRaiz(),r.getFechaInicio(),r.getFechaFin());}
    VersionDocumentoXsd toEntity(VersionDocumentoXsdModel model);
    default ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd documentoXsd(Long id){if(id==null)return null;var x=new ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd();x.setId(id);return x;}
}
