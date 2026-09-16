package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.EnumeracionXsdRequest;
import ec.dalara.factucore.application.contract.response.EnumeracionXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class EnumeracionXsdMapperImpl implements EnumeracionXsdMapper {

    @Override
    public EnumeracionXsd toEntity(EnumeracionXsdRequest request) {
        if ( request == null ) {
            return null;
        }

        EnumeracionXsd.EnumeracionXsdBuilder enumeracionXsd = EnumeracionXsd.builder();

        enumeracionXsd.elementoXsd( mapElementoXsd( request.getIdElementoXsd() ) );
        enumeracionXsd.valor( request.getValor() );
        enumeracionXsd.descripcion( request.getDescripcion() );
        enumeracionXsd.orden( request.getOrden() );

        return enumeracionXsd.build();
    }

    @Override
    public EnumeracionXsdResponse toResponse(EnumeracionXsd entity) {
        if ( entity == null ) {
            return null;
        }

        EnumeracionXsdResponse.EnumeracionXsdResponseBuilder enumeracionXsdResponse = EnumeracionXsdResponse.builder();

        enumeracionXsdResponse.idElementoXsd( entityElementoXsdId( entity ) );
        enumeracionXsdResponse.id( entity.getId() );
        enumeracionXsdResponse.valor( entity.getValor() );
        enumeracionXsdResponse.descripcion( entity.getDescripcion() );
        enumeracionXsdResponse.orden( entity.getOrden() );
        enumeracionXsdResponse.estadoRegistro( entity.getEstadoRegistro() );
        enumeracionXsdResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        enumeracionXsdResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        enumeracionXsdResponse.fechaCreacion( entity.getFechaCreacion() );
        enumeracionXsdResponse.fechaModificacion( entity.getFechaModificacion() );
        enumeracionXsdResponse.observacion( entity.getObservacion() );

        return enumeracionXsdResponse.build();
    }

    private Long entityElementoXsdId(EnumeracionXsd enumeracionXsd) {
        ElementoXsd elementoXsd = enumeracionXsd.getElementoXsd();
        if ( elementoXsd == null ) {
            return null;
        }
        return elementoXsd.getId();
    }
}
