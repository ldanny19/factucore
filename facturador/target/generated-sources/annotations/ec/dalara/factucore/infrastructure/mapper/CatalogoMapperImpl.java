package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CatalogoRequest;
import ec.dalara.factucore.application.contract.response.CatalogoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class CatalogoMapperImpl implements CatalogoMapper {

    @Override
    public Catalogo toEntity(CatalogoRequest request) {
        if ( request == null ) {
            return null;
        }

        Catalogo.CatalogoBuilder catalogo = Catalogo.builder();

        catalogo.codigo( request.getCodigo() );
        catalogo.nombre( request.getNombre() );
        catalogo.descripcion( request.getDescripcion() );

        return catalogo.build();
    }

    @Override
    public CatalogoResponse toResponse(Catalogo entity) {
        if ( entity == null ) {
            return null;
        }

        CatalogoResponse.CatalogoResponseBuilder catalogoResponse = CatalogoResponse.builder();

        catalogoResponse.id( entity.getId() );
        catalogoResponse.codigo( entity.getCodigo() );
        catalogoResponse.nombre( entity.getNombre() );
        catalogoResponse.descripcion( entity.getDescripcion() );
        catalogoResponse.estadoRegistro( entity.getEstadoRegistro() );
        catalogoResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        catalogoResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        catalogoResponse.fechaCreacion( entity.getFechaCreacion() );
        catalogoResponse.fechaModificacion( entity.getFechaModificacion() );
        catalogoResponse.observacion( entity.getObservacion() );

        return catalogoResponse.build();
    }
}
