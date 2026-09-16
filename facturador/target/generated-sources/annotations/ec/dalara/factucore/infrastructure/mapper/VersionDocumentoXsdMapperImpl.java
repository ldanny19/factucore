package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.VersionDocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.VersionDocumentoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class VersionDocumentoXsdMapperImpl implements VersionDocumentoXsdMapper {

    @Override
    public VersionDocumentoXsd toEntity(VersionDocumentoXsdRequest request) {
        if ( request == null ) {
            return null;
        }

        VersionDocumentoXsd.VersionDocumentoXsdBuilder versionDocumentoXsd = VersionDocumentoXsd.builder();

        versionDocumentoXsd.documentoXsd( mapDocumentoXsd( request.getIdDocumentoXsd() ) );
        versionDocumentoXsd.version( request.getVersion() );
        versionDocumentoXsd.nombreArchivo( request.getNombreArchivo() );
        versionDocumentoXsd.rutaXsd( request.getRutaXsd() );
        versionDocumentoXsd.namespaceXml( request.getNamespaceXml() );
        versionDocumentoXsd.elementoRaiz( request.getElementoRaiz() );
        versionDocumentoXsd.plantillaJson( request.getPlantillaJson() );
        versionDocumentoXsd.esquemaJson( request.getEsquemaJson() );
        versionDocumentoXsd.fechaInicio( request.getFechaInicio() );
        versionDocumentoXsd.fechaFin( request.getFechaFin() );

        return versionDocumentoXsd.build();
    }

    @Override
    public VersionDocumentoXsdResponse toResponse(VersionDocumentoXsd entity) {
        if ( entity == null ) {
            return null;
        }

        VersionDocumentoXsdResponse.VersionDocumentoXsdResponseBuilder versionDocumentoXsdResponse = VersionDocumentoXsdResponse.builder();

        versionDocumentoXsdResponse.idDocumentoXsd( entityDocumentoXsdId( entity ) );
        versionDocumentoXsdResponse.id( entity.getId() );
        versionDocumentoXsdResponse.version( entity.getVersion() );
        versionDocumentoXsdResponse.nombreArchivo( entity.getNombreArchivo() );
        versionDocumentoXsdResponse.rutaXsd( entity.getRutaXsd() );
        versionDocumentoXsdResponse.namespaceXml( entity.getNamespaceXml() );
        versionDocumentoXsdResponse.elementoRaiz( entity.getElementoRaiz() );
        versionDocumentoXsdResponse.plantillaJson( entity.getPlantillaJson() );
        versionDocumentoXsdResponse.esquemaJson( entity.getEsquemaJson() );
        versionDocumentoXsdResponse.fechaInicio( entity.getFechaInicio() );
        versionDocumentoXsdResponse.fechaFin( entity.getFechaFin() );
        versionDocumentoXsdResponse.estadoRegistro( entity.getEstadoRegistro() );
        versionDocumentoXsdResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        versionDocumentoXsdResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        versionDocumentoXsdResponse.fechaCreacion( entity.getFechaCreacion() );
        versionDocumentoXsdResponse.fechaModificacion( entity.getFechaModificacion() );
        versionDocumentoXsdResponse.observacion( entity.getObservacion() );

        return versionDocumentoXsdResponse.build();
    }

    private Long entityDocumentoXsdId(VersionDocumentoXsd versionDocumentoXsd) {
        DocumentoXsd documentoXsd = versionDocumentoXsd.getDocumentoXsd();
        if ( documentoXsd == null ) {
            return null;
        }
        return documentoXsd.getId();
    }
}
