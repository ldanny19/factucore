package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.AtributoXsdRequest;
import ec.dalara.factucore.application.contract.response.AtributoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class AtributoXsdMapperImpl implements AtributoXsdMapper {

    @Override
    public AtributoXsd toEntity(AtributoXsdRequest request) {
        if ( request == null ) {
            return null;
        }

        AtributoXsd.AtributoXsdBuilder atributoXsd = AtributoXsd.builder();

        atributoXsd.elementoXsd( mapElementoXsd( request.getIdElementoXsd() ) );
        atributoXsd.nombre( request.getNombre() );
        atributoXsd.tipoDato( request.getTipoDato() );
        atributoXsd.obligatorio( request.getObligatorio() );
        atributoXsd.valorPredeterminado( request.getValorPredeterminado() );
        atributoXsd.patron( request.getPatron() );

        return atributoXsd.build();
    }

    @Override
    public AtributoXsdResponse toResponse(AtributoXsd entity) {
        if ( entity == null ) {
            return null;
        }

        AtributoXsdResponse.AtributoXsdResponseBuilder atributoXsdResponse = AtributoXsdResponse.builder();

        atributoXsdResponse.idElementoXsd( entityElementoXsdId( entity ) );
        atributoXsdResponse.id( entity.getId() );
        atributoXsdResponse.nombre( entity.getNombre() );
        atributoXsdResponse.tipoDato( entity.getTipoDato() );
        atributoXsdResponse.obligatorio( entity.getObligatorio() );
        atributoXsdResponse.valorPredeterminado( entity.getValorPredeterminado() );
        atributoXsdResponse.patron( entity.getPatron() );
        atributoXsdResponse.estadoRegistro( entity.getEstadoRegistro() );
        atributoXsdResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        atributoXsdResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        atributoXsdResponse.fechaCreacion( entity.getFechaCreacion() );
        atributoXsdResponse.fechaModificacion( entity.getFechaModificacion() );
        atributoXsdResponse.observacion( entity.getObservacion() );

        return atributoXsdResponse.build();
    }

    private Long entityElementoXsdId(AtributoXsd atributoXsd) {
        ElementoXsd elementoXsd = atributoXsd.getElementoXsd();
        if ( elementoXsd == null ) {
            return null;
        }
        return elementoXsd.getId();
    }
}
