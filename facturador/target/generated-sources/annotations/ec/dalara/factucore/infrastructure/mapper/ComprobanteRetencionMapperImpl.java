package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteRetencionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteRetencionResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteRetencionMapperImpl implements ComprobanteRetencionMapper {

    @Override
    public ComprobanteRetencion toEntity(ComprobanteRetencionRequest request) {
        if ( request == null ) {
            return null;
        }

        ComprobanteRetencion.ComprobanteRetencionBuilder comprobanteRetencion = ComprobanteRetencion.builder();

        comprobanteRetencion.comprobante( mapComprobante( request.getIdComprobante() ) );
        comprobanteRetencion.codigoImpuesto( request.getCodigoImpuesto() );
        comprobanteRetencion.codigoRetencion( request.getCodigoRetencion() );
        comprobanteRetencion.porcentajeRetener( request.getPorcentajeRetener() );
        comprobanteRetencion.baseImponible( request.getBaseImponible() );
        comprobanteRetencion.valorRetenido( request.getValorRetenido() );
        comprobanteRetencion.numeroDocumentoSustento( request.getNumeroDocumentoSustento() );
        comprobanteRetencion.fechaEmisionDocumentoSustento( request.getFechaEmisionDocumentoSustento() );

        return comprobanteRetencion.build();
    }

    @Override
    public ComprobanteRetencionResponse toResponse(ComprobanteRetencion entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteRetencionResponse.ComprobanteRetencionResponseBuilder comprobanteRetencionResponse = ComprobanteRetencionResponse.builder();

        comprobanteRetencionResponse.idComprobante( entityComprobanteId( entity ) );
        comprobanteRetencionResponse.id( entity.getId() );
        comprobanteRetencionResponse.codigoImpuesto( entity.getCodigoImpuesto() );
        comprobanteRetencionResponse.codigoRetencion( entity.getCodigoRetencion() );
        comprobanteRetencionResponse.porcentajeRetener( entity.getPorcentajeRetener() );
        comprobanteRetencionResponse.baseImponible( entity.getBaseImponible() );
        comprobanteRetencionResponse.valorRetenido( entity.getValorRetenido() );
        comprobanteRetencionResponse.numeroDocumentoSustento( entity.getNumeroDocumentoSustento() );
        comprobanteRetencionResponse.fechaEmisionDocumentoSustento( entity.getFechaEmisionDocumentoSustento() );
        comprobanteRetencionResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteRetencionResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteRetencionResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteRetencionResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteRetencionResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteRetencionResponse.observacion( entity.getObservacion() );

        return comprobanteRetencionResponse.build();
    }

    private Long entityComprobanteId(ComprobanteRetencion comprobanteRetencion) {
        Comprobante comprobante = comprobanteRetencion.getComprobante();
        if ( comprobante == null ) {
            return null;
        }
        return comprobante.getId();
    }
}
