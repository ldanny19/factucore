package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ElementoXsdRequest;
import ec.dalara.factucore.application.contract.response.ElementoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ElementoXsdMapperImpl implements ElementoXsdMapper {

    @Override
    public ElementoXsd toEntity(ElementoXsdRequest request) {
        if ( request == null ) {
            return null;
        }

        ElementoXsd.ElementoXsdBuilder elementoXsd = ElementoXsd.builder();

        elementoXsd.versionDocumentoXsd( mapVersionDocumentoXsd( request.getIdVersionDocumentoXsd() ) );
        elementoXsd.elementoPadre( mapElementoPadre( request.getIdElementoPadre() ) );
        elementoXsd.nombre( request.getNombre() );
        elementoXsd.tipoDato( request.getTipoDato() );
        elementoXsd.orden( request.getOrden() );
        elementoXsd.obligatorio( request.getObligatorio() );
        elementoXsd.repetible( request.getRepetible() );
        elementoXsd.minOcurrencias( request.getMinOcurrencias() );
        elementoXsd.maxOcurrencias( request.getMaxOcurrencias() );
        elementoXsd.longitudMinima( request.getLongitudMinima() );
        elementoXsd.longitudMaxima( request.getLongitudMaxima() );
        elementoXsd.digitosTotales( request.getDigitosTotales() );
        elementoXsd.decimales( request.getDecimales() );
        elementoXsd.valorMinimo( request.getValorMinimo() );
        elementoXsd.valorMaximo( request.getValorMaximo() );
        elementoXsd.patron( request.getPatron() );
        elementoXsd.fechaInicio( request.getFechaInicio() );
        elementoXsd.fechaFin( request.getFechaFin() );

        return elementoXsd.build();
    }

    @Override
    public ElementoXsdResponse toResponse(ElementoXsd entity) {
        if ( entity == null ) {
            return null;
        }

        ElementoXsdResponse.ElementoXsdResponseBuilder elementoXsdResponse = ElementoXsdResponse.builder();

        elementoXsdResponse.idVersionDocumentoXsd( entityVersionDocumentoXsdId( entity ) );
        elementoXsdResponse.idElementoPadre( entityElementoPadreId( entity ) );
        elementoXsdResponse.id( entity.getId() );
        elementoXsdResponse.nombre( entity.getNombre() );
        elementoXsdResponse.tipoDato( entity.getTipoDato() );
        elementoXsdResponse.orden( entity.getOrden() );
        elementoXsdResponse.obligatorio( entity.getObligatorio() );
        elementoXsdResponse.repetible( entity.getRepetible() );
        elementoXsdResponse.minOcurrencias( entity.getMinOcurrencias() );
        elementoXsdResponse.maxOcurrencias( entity.getMaxOcurrencias() );
        elementoXsdResponse.longitudMinima( entity.getLongitudMinima() );
        elementoXsdResponse.longitudMaxima( entity.getLongitudMaxima() );
        elementoXsdResponse.digitosTotales( entity.getDigitosTotales() );
        elementoXsdResponse.decimales( entity.getDecimales() );
        elementoXsdResponse.valorMinimo( entity.getValorMinimo() );
        elementoXsdResponse.valorMaximo( entity.getValorMaximo() );
        elementoXsdResponse.patron( entity.getPatron() );
        elementoXsdResponse.fechaInicio( entity.getFechaInicio() );
        elementoXsdResponse.fechaFin( entity.getFechaFin() );
        elementoXsdResponse.estadoRegistro( entity.getEstadoRegistro() );
        elementoXsdResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        elementoXsdResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        elementoXsdResponse.fechaCreacion( entity.getFechaCreacion() );
        elementoXsdResponse.fechaModificacion( entity.getFechaModificacion() );
        elementoXsdResponse.observacion( entity.getObservacion() );

        return elementoXsdResponse.build();
    }

    private Long entityVersionDocumentoXsdId(ElementoXsd elementoXsd) {
        VersionDocumentoXsd versionDocumentoXsd = elementoXsd.getVersionDocumentoXsd();
        if ( versionDocumentoXsd == null ) {
            return null;
        }
        return versionDocumentoXsd.getId();
    }

    private Long entityElementoPadreId(ElementoXsd elementoXsd) {
        ElementoXsd elementoPadre = elementoXsd.getElementoPadre();
        if ( elementoPadre == null ) {
            return null;
        }
        return elementoPadre.getId();
    }
}
