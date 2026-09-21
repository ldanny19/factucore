package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobantePagoRequest;
import ec.dalara.factucore.application.contract.response.ComprobantePagoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobantePagoMapperImpl implements ComprobantePagoMapper {

    @Override
    public ComprobantePago toEntity(ComprobantePagoRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobantePago.ComprobantePagoBuilder comprobantePago = ComprobantePago.builder();

        comprobantePago.comprobante( mapComprobante( request.getIdComprobante() ) );
        comprobantePago.codigoFormaPago( request.getCodigoFormaPago() );
        comprobantePago.total( request.getTotal() );
        comprobantePago.plazo( request.getPlazo() );
        comprobantePago.unidadTiempo( request.getUnidadTiempo() );

        return comprobantePago.build();
    }

    @Override
    public ComprobantePagoResponse toResponse(ComprobantePago entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobantePagoResponse.ComprobantePagoResponseBuilder comprobantePagoResponse = ComprobantePagoResponse.builder();

        comprobantePagoResponse.idComprobante( entityComprobanteId( entity ) );
        comprobantePagoResponse.id( entity.getId() );
        comprobantePagoResponse.codigoFormaPago( entity.getCodigoFormaPago() );
        comprobantePagoResponse.total( entity.getTotal() );
        comprobantePagoResponse.plazo( entity.getPlazo() );
        comprobantePagoResponse.unidadTiempo( entity.getUnidadTiempo() );
        comprobantePagoResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobantePagoResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobantePagoResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobantePagoResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobantePagoResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobantePagoResponse.observacion( entity.getObservacion() );

        return comprobantePagoResponse.build();
    }

    private Long entityComprobanteId(ComprobantePago comprobantePago) {
        Comprobante comprobante = comprobantePago.getComprobante();
        if ( comprobante == null ) {
            return null;
        }
        return comprobante.getId();
    }
}
