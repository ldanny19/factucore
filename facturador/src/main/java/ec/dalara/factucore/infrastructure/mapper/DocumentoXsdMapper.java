package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DocumentoXsdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    DocumentoXsd toEntity(DocumentoXsdRequest request);

    DocumentoXsdResponse toResponse(DocumentoXsd entity);
}