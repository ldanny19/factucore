package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface DocumentoXsdMapper {
    DocumentoXsdModel toModel(DocumentoXsdRequest request);
    DocumentoXsdResponse toResponse(DocumentoXsdModel model);
}