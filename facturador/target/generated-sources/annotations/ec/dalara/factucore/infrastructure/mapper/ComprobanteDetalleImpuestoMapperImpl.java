package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteDetalleImpuestoRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteDetalleImpuestoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteDetalleImpuestoMapperImpl implements ComprobanteDetalleImpuestoMapper {

    @Override
    public ComprobanteDetalleImpuesto toEntity(ComprobanteDetalleImpuestoRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobanteDetalleImpuesto.ComprobanteDetalleImpuestoBuilder comprobanteDetalleImpuesto = ComprobanteDetalleImpuesto.builder();

        comprobanteDetalleImpuesto.comprobanteDetalle( mapComprobanteDetalle( request.getIdComprobanteDetalle() ) );
        comprobanteDetalleImpuesto.codigoImpuesto( request.getCodigoImpuesto() );
        comprobanteDetalleImpuesto.codigoPorcentaje( request.getCodigoPorcentaje() );
        comprobanteDetalleImpuesto.tarifa( request.getTarifa() );
        comprobanteDetalleImpuesto.baseImponible( request.getBaseImponible() );
        comprobanteDetalleImpuesto.valor( request.getValor() );
        comprobanteDetalleImpuesto.valorDevolucionIva( request.getValorDevolucionIva() );

        return comprobanteDetalleImpuesto.build();
    }

    @Override
    public ComprobanteDetalleImpuestoResponse toResponse(ComprobanteDetalleImpuesto entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteDetalleImpuestoResponse.ComprobanteDetalleImpuestoResponseBuilder comprobanteDetalleImpuestoResponse = ComprobanteDetalleImpuestoResponse.builder();

        comprobanteDetalleImpuestoResponse.idComprobanteDetalle( entityComprobanteDetalleId( entity ) );
        comprobanteDetalleImpuestoResponse.id( entity.getId() );
        comprobanteDetalleImpuestoResponse.codigoImpuesto( entity.getCodigoImpuesto() );
        comprobanteDetalleImpuestoResponse.codigoPorcentaje( entity.getCodigoPorcentaje() );
        comprobanteDetalleImpuestoResponse.tarifa( entity.getTarifa() );
        comprobanteDetalleImpuestoResponse.baseImponible( entity.getBaseImponible() );
        comprobanteDetalleImpuestoResponse.valor( entity.getValor() );
        comprobanteDetalleImpuestoResponse.valorDevolucionIva( entity.getValorDevolucionIva() );
        comprobanteDetalleImpuestoResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteDetalleImpuestoResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteDetalleImpuestoResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteDetalleImpuestoResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteDetalleImpuestoResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteDetalleImpuestoResponse.observacion( entity.getObservacion() );

        return comprobanteDetalleImpuestoResponse.build();
    }

    private Long entityComprobanteDetalleId(ComprobanteDetalleImpuesto comprobanteDetalleImpuesto) {
        ComprobanteDetalle comprobanteDetalle = comprobanteDetalleImpuesto.getComprobanteDetalle();
        if ( comprobanteDetalle == null ) {
            return null;
        }
        return comprobanteDetalle.getId();
    }
}
