package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface DocumentoXsdEntityMapper {
 default DocumentoXsdModel toModel(DocumentoXsd e){if(e==null)return null;return new DocumentoXsdModel(e.getCodigo(), e.getNombre(), e.getDescripcion(), e.getTipoDocumento(), e.getPrefijoArchivo());}
 @Mapping(target="id",ignore=true)
 
 DocumentoXsd toEntity(DocumentoXsdModel model);
}