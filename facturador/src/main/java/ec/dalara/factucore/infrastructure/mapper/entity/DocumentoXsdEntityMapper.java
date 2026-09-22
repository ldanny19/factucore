package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface DocumentoXsdEntityMapper {
    default DocumentoXsdModel toModel(DocumentoXsd r){if(r==null)return null;return new DocumentoXsdModel(r.getCodigo(),r.getNombre(),r.getDescripcion(),r.getTipoDocumento());}
    DocumentoXsd toEntity(DocumentoXsdModel model);
}
