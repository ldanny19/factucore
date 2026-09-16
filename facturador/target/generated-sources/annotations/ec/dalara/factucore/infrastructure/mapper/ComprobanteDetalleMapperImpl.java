package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteDetalleRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteDetalleMapperImpl implements ComprobanteDetalleMapper {

    @Override
    public ComprobanteDetalle toEntity(ComprobanteDetalleRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobanteDetalle.ComprobanteDetalleBuilder comprobanteDetalle = ComprobanteDetalle.builder();

        comprobanteDetalle.comprobante( mapComprobante( request.getIdComprobante() ) );
        comprobanteDetalle.numeroLinea( request.getOrden() );
        comprobanteDetalle.codigoPrincipal( request.getCodigoPrincipal() );
        comprobanteDetalle.codigoAuxiliar( request.getCodigoAuxiliar() );
        comprobanteDetalle.descripcion( request.getDescripcion() );
        comprobanteDetalle.cantidad( request.getCantidad() );
        comprobanteDetalle.precioUnitario( request.getPrecioUnitario() );
        comprobanteDetalle.descuento( request.getDescuento() );
        comprobanteDetalle.precioTotalSinImpuesto( request.getPrecioTotalSinImpuesto() );

        return comprobanteDetalle.build();
    }

    @Override
    public ComprobanteDetalleResponse toResponse(ComprobanteDetalle entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteDetalleResponse.ComprobanteDetalleResponseBuilder comprobanteDetalleResponse = ComprobanteDetalleResponse.builder();

        comprobanteDetalleResponse.idComprobante( entityComprobanteId( entity ) );
        comprobanteDetalleResponse.orden( entity.getNumeroLinea() );
        comprobanteDetalleResponse.id( entity.getId() );
        comprobanteDetalleResponse.codigoPrincipal( entity.getCodigoPrincipal() );
        comprobanteDetalleResponse.codigoAuxiliar( entity.getCodigoAuxiliar() );
        comprobanteDetalleResponse.descripcion( entity.getDescripcion() );
        comprobanteDetalleResponse.cantidad( entity.getCantidad() );
        comprobanteDetalleResponse.precioUnitario( entity.getPrecioUnitario() );
        comprobanteDetalleResponse.descuento( entity.getDescuento() );
        comprobanteDetalleResponse.precioTotalSinImpuesto( entity.getPrecioTotalSinImpuesto() );
        comprobanteDetalleResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteDetalleResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteDetalleResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteDetalleResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteDetalleResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteDetalleResponse.observacion( entity.getObservacion() );

        return comprobanteDetalleResponse.build();
    }

    private Long entityComprobanteId(ComprobanteDetalle comprobanteDetalle) {
        Comprobante comprobante = comprobanteDetalle.getComprobante();
        if ( comprobante == null ) {
            return null;
        }
        return comprobante.getId();
    }
}
