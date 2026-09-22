package ec.dalara.factucore.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.domain.certificadofirma.CertificadoFirmaModel;
@Mapper(config=MapStructApplicationConfig.class)
public interface CertificadoFirmaMapper {
    CertificadoFirmaModel toModel(CertificadoFirmaRequest request);
    CertificadoFirmaResponse toResponse(CertificadoFirmaModel model);
}