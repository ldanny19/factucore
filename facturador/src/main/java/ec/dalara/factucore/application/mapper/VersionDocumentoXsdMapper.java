package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.VersionDocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.VersionDocumentoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface VersionDocumentoXsdMapper {
    default VersionDocumentoXsdModel toModel(VersionDocumentoXsdRequest r) { if(r==null)return null; return new VersionDocumentoXsdModel(null,r.getIdDocumentoXsd(),r.getVersion(),r.getNamespaceXml(),r.getElementoRaiz(),r.getFechaInicio(),r.getFechaFin()); }
    VersionDocumentoXsdResponse toResponse(VersionDocumentoXsdModel model);
}