package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import ec.dalara.factucore.application.contract.request.MapeoXsdRequest;
import ec.dalara.factucore.application.contract.response.MapeoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface MapeoXsdMapper {
 default MapeoXsdModel toModel(MapeoXsdRequest r){if(r==null)return null;return new MapeoXsdModel(null,r.getIdVersionDocumentoXsd(),r.getRutaOrigen(),r.getIdElementoXsd(),r.getIdAtributoXsd(),r.getTipoMapeo());}
 MapeoXsdResponse toResponse(MapeoXsdModel model);
}