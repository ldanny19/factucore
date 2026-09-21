package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class CertificadoFirmaMapperImpl implements CertificadoFirmaMapper {

    @Override
    public CertificadoFirma toEntity(CertificadoFirmaRequest request) {
        if ( request == null ) {
            return null;
        }

        CertificadoFirma.CertificadoFirmaBuilder certificadoFirma = CertificadoFirma.builder();

        certificadoFirma.empresa( mapEmpresa( request.getIdEmpresa() ) );
        certificadoFirma.nombreArchivo( request.getNombreArchivo() );
        certificadoFirma.rutaCertificado( request.getRutaCertificado() );
        certificadoFirma.fechaInicio( request.getFechaInicio() );
        certificadoFirma.fechaFin( request.getFechaFin() );

        return certificadoFirma.build();
    }

    @Override
    public CertificadoFirmaResponse toResponse(CertificadoFirma entity) {
        if ( entity == null ) {
            return null;
        }

        CertificadoFirmaResponse.CertificadoFirmaResponseBuilder certificadoFirmaResponse = CertificadoFirmaResponse.builder();

        certificadoFirmaResponse.idEmpresa( entityEmpresaId( entity ) );
        certificadoFirmaResponse.id( entity.getId() );
        certificadoFirmaResponse.nombreArchivo( entity.getNombreArchivo() );
        certificadoFirmaResponse.rutaCertificado( entity.getRutaCertificado() );
        certificadoFirmaResponse.fechaInicio( entity.getFechaInicio() );
        certificadoFirmaResponse.fechaFin( entity.getFechaFin() );
        certificadoFirmaResponse.estadoRegistro( entity.getEstadoRegistro() );
        certificadoFirmaResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        certificadoFirmaResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        certificadoFirmaResponse.fechaCreacion( entity.getFechaCreacion() );
        certificadoFirmaResponse.fechaModificacion( entity.getFechaModificacion() );
        certificadoFirmaResponse.observacion( entity.getObservacion() );

        return certificadoFirmaResponse.build();
    }

    private Long entityEmpresaId(CertificadoFirma certificadoFirma) {
        Empresa empresa = certificadoFirma.getEmpresa();
        if ( empresa == null ) {
            return null;
        }
        return empresa.getId();
    }
}
