package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteInformacionAdicionalRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteInformacionAdicionalResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteInformacionAdicionalMapperImpl implements ComprobanteInformacionAdicionalMapper {

    @Override
    public ComprobanteInformacionAdicional toEntity(ComprobanteInformacionAdicionalRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobanteInformacionAdicional.ComprobanteInformacionAdicionalBuilder comprobanteInformacionAdicional = ComprobanteInformacionAdicional.builder();

        comprobanteInformacionAdicional.comprobante( mapComprobante( request.getIdComprobante() ) );
        comprobanteInformacionAdicional.nombre( request.getNombre() );
        comprobanteInformacionAdicional.valor( request.getValor() );

        return comprobanteInformacionAdicional.build();
    }

    @Override
    public ComprobanteInformacionAdicionalResponse toResponse(ComprobanteInformacionAdicional entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteInformacionAdicionalResponse.ComprobanteInformacionAdicionalResponseBuilder comprobanteInformacionAdicionalResponse = ComprobanteInformacionAdicionalResponse.builder();

        comprobanteInformacionAdicionalResponse.idComprobante( entityComprobanteId( entity ) );
        comprobanteInformacionAdicionalResponse.id( entity.getId() );
        comprobanteInformacionAdicionalResponse.nombre( entity.getNombre() );
        comprobanteInformacionAdicionalResponse.valor( entity.getValor() );
        comprobanteInformacionAdicionalResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteInformacionAdicionalResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteInformacionAdicionalResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteInformacionAdicionalResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteInformacionAdicionalResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteInformacionAdicionalResponse.observacion( entity.getObservacion() );

        return comprobanteInformacionAdicionalResponse.build();
    }

    private Long entityComprobanteId(ComprobanteInformacionAdicional comprobanteInformacionAdicional) {
        Comprobante comprobante = comprobanteInformacionAdicional.getComprobante();
        if ( comprobante == null ) {
            return null;
        }
        return comprobante.getId();
    }
}
