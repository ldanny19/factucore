package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class DocumentoXsdMapperImpl implements DocumentoXsdMapper {

    @Override
    public DocumentoXsd toEntity(DocumentoXsdRequest request) {
        if ( request == null ) {
            return null;
        }

        DocumentoXsd.DocumentoXsdBuilder documentoXsd = DocumentoXsd.builder();

        documentoXsd.codigo( request.getCodigo() );
        documentoXsd.nombre( request.getNombre() );
        documentoXsd.descripcion( request.getDescripcion() );
        documentoXsd.tipoDocumento( request.getTipoDocumento() );

        return documentoXsd.build();
    }

    @Override
    public DocumentoXsdResponse toResponse(DocumentoXsd entity) {
        if ( entity == null ) {
            return null;
        }

        DocumentoXsdResponse.DocumentoXsdResponseBuilder documentoXsdResponse = DocumentoXsdResponse.builder();

        documentoXsdResponse.id( entity.getId() );
        documentoXsdResponse.codigo( entity.getCodigo() );
        documentoXsdResponse.nombre( entity.getNombre() );
        documentoXsdResponse.descripcion( entity.getDescripcion() );
        documentoXsdResponse.tipoDocumento( entity.getTipoDocumento() );
        documentoXsdResponse.estadoRegistro( entity.getEstadoRegistro() );
        documentoXsdResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        documentoXsdResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        documentoXsdResponse.fechaCreacion( entity.getFechaCreacion() );
        documentoXsdResponse.fechaModificacion( entity.getFechaModificacion() );
        documentoXsdResponse.observacion( entity.getObservacion() );

        return documentoXsdResponse.build();
    }
}
