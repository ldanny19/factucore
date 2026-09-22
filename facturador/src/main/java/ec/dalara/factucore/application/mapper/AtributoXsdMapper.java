package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.AtributoXsdRequest;
import ec.dalara.factucore.application.contract.response.AtributoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface AtributoXsdMapper {
 default AtributoXsdModel toModel(AtributoXsdRequest r){if(r==null)return null;return new AtributoXsdModel(null,r.getIdElementoXsd(),r.getNombre(),r.getTipoDato(),r.getObligatorio(),r.getValorPredeterminado(),r.getPatron());}
 AtributoXsdResponse toResponse(AtributoXsdModel model);
}