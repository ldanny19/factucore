package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class SecuencialMapperImpl implements SecuencialMapper {

    @Override
    public Secuencial toEntity(SecuencialRequest request) {
        if ( request == null ) {
            return null;
        }

        Secuencial.SecuencialBuilder secuencial = Secuencial.builder();

        secuencial.puntoEmision( mapPuntoEmision( request.getIdPuntoEmision() ) );
        secuencial.codigoDocumento( request.getCodigoDocumento() );
        secuencial.ultimoSecuencial( request.getUltimoSecuencial() );

        return secuencial.build();
    }

    @Override
    public SecuencialResponse toResponse(Secuencial entity) {
        if ( entity == null ) {
            return null;
        }

        SecuencialResponse.SecuencialResponseBuilder secuencialResponse = SecuencialResponse.builder();

        secuencialResponse.idPuntoEmision( entityPuntoEmisionId( entity ) );
        secuencialResponse.id( entity.getId() );
        secuencialResponse.codigoDocumento( entity.getCodigoDocumento() );
        secuencialResponse.ultimoSecuencial( entity.getUltimoSecuencial() );
        secuencialResponse.estadoRegistro( entity.getEstadoRegistro() );
        secuencialResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        secuencialResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        secuencialResponse.fechaCreacion( entity.getFechaCreacion() );
        secuencialResponse.fechaModificacion( entity.getFechaModificacion() );
        secuencialResponse.observacion( entity.getObservacion() );

        return secuencialResponse.build();
    }

    private Long entityPuntoEmisionId(Secuencial secuencial) {
        PuntoEmision puntoEmision = secuencial.getPuntoEmision();
        if ( puntoEmision == null ) {
            return null;
        }
        return puntoEmision.getId();
    }
}
