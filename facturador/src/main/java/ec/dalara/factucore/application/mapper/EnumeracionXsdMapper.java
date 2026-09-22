package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.EnumeracionXsdRequest;
import ec.dalara.factucore.application.contract.response.EnumeracionXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface EnumeracionXsdMapper {
 default EnumeracionXsdModel toModel(EnumeracionXsdRequest r){if(r==null)return null;return new EnumeracionXsdModel(r.getIdElementoXsd(),r.getValor(),r.getDescripcion(),r.getOrden());}
 EnumeracionXsdResponse toResponse(EnumeracionXsdModel model);
}