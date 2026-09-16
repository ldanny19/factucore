package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CertificadoFirmaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", source = "idEmpresa")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    CertificadoFirma toEntity(CertificadoFirmaRequest request);

    @Mapping(target = "idEmpresa", source = "empresa.id")
    CertificadoFirmaResponse toResponse(CertificadoFirma entity);

    default Empresa mapEmpresa(Long idEmpresa) {
        if (idEmpresa == null) {
            return null;
        }

        Empresa empresa = new Empresa();
        empresa.setId(idEmpresa);
        return empresa;
    }
}