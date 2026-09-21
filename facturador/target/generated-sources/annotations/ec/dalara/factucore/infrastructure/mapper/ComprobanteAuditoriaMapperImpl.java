package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteAuditoriaRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteAuditoriaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteAuditoriaMapperImpl implements ComprobanteAuditoriaMapper {

    @Override
    public ComprobanteAuditoria toEntity(ComprobanteAuditoriaRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobanteAuditoria.ComprobanteAuditoriaBuilder comprobanteAuditoria = ComprobanteAuditoria.builder();

        comprobanteAuditoria.comprobante( mapComprobante( request.getIdComprobante() ) );
        comprobanteAuditoria.estadoAnterior( request.getEstadoAnterior() );
        comprobanteAuditoria.estadoNuevo( request.getEstadoNuevo() );
        comprobanteAuditoria.codigoError( request.getCodigoError() );
        comprobanteAuditoria.mensajeError( request.getMensajeError() );

        return comprobanteAuditoria.build();
    }

    @Override
    public ComprobanteAuditoriaResponse toResponse(ComprobanteAuditoria entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteAuditoriaResponse.ComprobanteAuditoriaResponseBuilder comprobanteAuditoriaResponse = ComprobanteAuditoriaResponse.builder();

        comprobanteAuditoriaResponse.idComprobante( entityComprobanteId( entity ) );
        comprobanteAuditoriaResponse.id( entity.getId() );
        comprobanteAuditoriaResponse.estadoAnterior( entity.getEstadoAnterior() );
        comprobanteAuditoriaResponse.estadoNuevo( entity.getEstadoNuevo() );
        comprobanteAuditoriaResponse.codigoError( entity.getCodigoError() );
        comprobanteAuditoriaResponse.mensajeError( entity.getMensajeError() );
        comprobanteAuditoriaResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteAuditoriaResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteAuditoriaResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteAuditoriaResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteAuditoriaResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteAuditoriaResponse.observacion( entity.getObservacion() );

        return comprobanteAuditoriaResponse.build();
    }

    private Long entityComprobanteId(ComprobanteAuditoria comprobanteAuditoria) {
        Comprobante comprobante = comprobanteAuditoria.getComprobante();
        if ( comprobante == null ) {
            return null;
        }
        return comprobante.getId();
    }
}
