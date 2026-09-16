package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ComprobanteRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ComprobanteMapperImpl implements ComprobanteMapper {

    @Override
    public Comprobante toEntity(ComprobanteRequest request) {
        if ( request == null ) {
            return null;
        }

        Comprobante.ComprobanteBuilder comprobante = Comprobante.builder();

        comprobante.empresa( mapEmpresa( request.getIdEmpresa() ) );
        comprobante.establecimiento( mapEstablecimiento( request.getIdEstablecimiento() ) );
        comprobante.puntoEmision( mapPuntoEmision( request.getIdPuntoEmision() ) );
        comprobante.documentoXsd( mapDocumentoXsd( request.getIdDocumentoXsd() ) );
        comprobante.versionDocumentoXsd( mapVersionDocumentoXsd( request.getIdVersionDocumentoXsd() ) );
        comprobante.ambiente( request.getAmbiente() );
        comprobante.tipoEmision( request.getTipoEmision() );
        comprobante.codigoDocumento( request.getCodigoDocumento() );
        comprobante.secuencial( request.getSecuencial() );
        comprobante.claveAcceso( request.getClaveAcceso() );
        comprobante.fechaEmision( request.getFechaEmision() );
        comprobante.razonSocialEmisor( request.getRazonSocialEmisor() );
        comprobante.nombreComercialEmisor( request.getNombreComercialEmisor() );
        comprobante.rucEmisor( request.getRucEmisor() );
        comprobante.direccionMatrizEmisor( request.getDireccionMatrizEmisor() );
        comprobante.direccionEstablecimientoEmisor( request.getDireccionEstablecimientoEmisor() );
        comprobante.identificacionReceptor( request.getIdentificacionReceptor() );
        comprobante.tipoIdentificacionReceptor( request.getTipoIdentificacionReceptor() );
        comprobante.razonSocialReceptor( request.getRazonSocialReceptor() );
        comprobante.direccionReceptor( request.getDireccionReceptor() );
        comprobante.estadoProceso( request.getEstadoProceso() );
        comprobante.codigoError( request.getCodigoError() );
        comprobante.mensajeError( request.getMensajeError() );
        comprobante.numeroAutorizacion( request.getNumeroAutorizacion() );
        comprobante.fechaAutorizacion( request.getFechaAutorizacion() );
        comprobante.rutaXmlFirmado( request.getRutaXmlFirmado() );
        comprobante.rutaRespuestaSri( request.getRutaRespuestaSri() );
        comprobante.rutaRide( request.getRutaRide() );

        return comprobante.build();
    }

    @Override
    public ComprobanteResponse toResponse(Comprobante entity) {
        if ( entity == null ) {
            return null;
        }

        ComprobanteResponse.ComprobanteResponseBuilder comprobanteResponse = ComprobanteResponse.builder();

        comprobanteResponse.idEmpresa( entityEmpresaId( entity ) );
        comprobanteResponse.idEstablecimiento( entityEstablecimientoId( entity ) );
        comprobanteResponse.idPuntoEmision( entityPuntoEmisionId( entity ) );
        comprobanteResponse.idDocumentoXsd( entityDocumentoXsdId( entity ) );
        comprobanteResponse.idVersionDocumentoXsd( entityVersionDocumentoXsdId( entity ) );
        comprobanteResponse.id( entity.getId() );
        comprobanteResponse.ambiente( entity.getAmbiente() );
        comprobanteResponse.tipoEmision( entity.getTipoEmision() );
        comprobanteResponse.codigoDocumento( entity.getCodigoDocumento() );
        comprobanteResponse.secuencial( entity.getSecuencial() );
        comprobanteResponse.claveAcceso( entity.getClaveAcceso() );
        comprobanteResponse.fechaEmision( entity.getFechaEmision() );
        comprobanteResponse.razonSocialEmisor( entity.getRazonSocialEmisor() );
        comprobanteResponse.nombreComercialEmisor( entity.getNombreComercialEmisor() );
        comprobanteResponse.rucEmisor( entity.getRucEmisor() );
        comprobanteResponse.direccionMatrizEmisor( entity.getDireccionMatrizEmisor() );
        comprobanteResponse.direccionEstablecimientoEmisor( entity.getDireccionEstablecimientoEmisor() );
        comprobanteResponse.identificacionReceptor( entity.getIdentificacionReceptor() );
        comprobanteResponse.tipoIdentificacionReceptor( entity.getTipoIdentificacionReceptor() );
        comprobanteResponse.razonSocialReceptor( entity.getRazonSocialReceptor() );
        comprobanteResponse.direccionReceptor( entity.getDireccionReceptor() );
        comprobanteResponse.estadoProceso( entity.getEstadoProceso() );
        comprobanteResponse.codigoError( entity.getCodigoError() );
        comprobanteResponse.mensajeError( entity.getMensajeError() );
        comprobanteResponse.numeroAutorizacion( entity.getNumeroAutorizacion() );
        comprobanteResponse.fechaAutorizacion( entity.getFechaAutorizacion() );
        comprobanteResponse.rutaXmlFirmado( entity.getRutaXmlFirmado() );
        comprobanteResponse.rutaRespuestaSri( entity.getRutaRespuestaSri() );
        comprobanteResponse.rutaRide( entity.getRutaRide() );
        comprobanteResponse.estadoRegistro( entity.getEstadoRegistro() );
        comprobanteResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        comprobanteResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        comprobanteResponse.fechaCreacion( entity.getFechaCreacion() );
        comprobanteResponse.fechaModificacion( entity.getFechaModificacion() );
        comprobanteResponse.observacion( entity.getObservacion() );

        return comprobanteResponse.build();
    }

    private Long entityEmpresaId(Comprobante comprobante) {
        Empresa empresa = comprobante.getEmpresa();
        if ( empresa == null ) {
            return null;
        }
        return empresa.getId();
    }

    private Long entityEstablecimientoId(Comprobante comprobante) {
        Establecimiento establecimiento = comprobante.getEstablecimiento();
        if ( establecimiento == null ) {
            return null;
        }
        return establecimiento.getId();
    }

    private Long entityPuntoEmisionId(Comprobante comprobante) {
        PuntoEmision puntoEmision = comprobante.getPuntoEmision();
        if ( puntoEmision == null ) {
            return null;
        }
        return puntoEmision.getId();
    }

    private Long entityDocumentoXsdId(Comprobante comprobante) {
        DocumentoXsd documentoXsd = comprobante.getDocumentoXsd();
        if ( documentoXsd == null ) {
            return null;
        }
        return documentoXsd.getId();
    }

    private Long entityVersionDocumentoXsdId(Comprobante comprobante) {
        VersionDocumentoXsd versionDocumentoXsd = comprobante.getVersionDocumentoXsd();
        if ( versionDocumentoXsd == null ) {
            return null;
        }
        return versionDocumentoXsd.getId();
    }
}
