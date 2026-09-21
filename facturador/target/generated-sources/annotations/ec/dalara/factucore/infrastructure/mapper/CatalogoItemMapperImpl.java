package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.CatalogoItemRequest;
import ec.dalara.factucore.application.contract.response.CatalogoItemResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class CatalogoItemMapperImpl implements CatalogoItemMapper {

    @Override
    public CatalogoItem toEntity(CatalogoItemRequest request) {
        if ( request == null ) {
            return null;
        }

        CatalogoItem.CatalogoItemBuilder catalogoItem = CatalogoItem.builder();

        catalogoItem.catalogo( mapCatalogo( request.getIdCatalogo() ) );
        catalogoItem.codigo( request.getCodigo() );
        catalogoItem.nombre( request.getNombre() );
        catalogoItem.descripcion( request.getDescripcion() );
        catalogoItem.orden( request.getOrden() );

        return catalogoItem.build();
    }

    @Override
    public CatalogoItemResponse toResponse(CatalogoItem entity) {
        if ( entity == null ) {
            return null;
        }

        CatalogoItemResponse.CatalogoItemResponseBuilder catalogoItemResponse = CatalogoItemResponse.builder();

        catalogoItemResponse.idCatalogo( entityCatalogoId( entity ) );
        catalogoItemResponse.id( entity.getId() );
        catalogoItemResponse.codigo( entity.getCodigo() );
        catalogoItemResponse.nombre( entity.getNombre() );
        catalogoItemResponse.descripcion( entity.getDescripcion() );
        catalogoItemResponse.orden( entity.getOrden() );
        catalogoItemResponse.estadoRegistro( entity.getEstadoRegistro() );
        catalogoItemResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        catalogoItemResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        catalogoItemResponse.fechaCreacion( entity.getFechaCreacion() );
        catalogoItemResponse.fechaModificacion( entity.getFechaModificacion() );
        catalogoItemResponse.observacion( entity.getObservacion() );

        return catalogoItemResponse.build();
    }

    private Long entityCatalogoId(CatalogoItem catalogoItem) {
        Catalogo catalogo = catalogoItem.getCatalogo();
        if ( catalogo == null ) {
            return null;
        }
        return catalogo.getId();
    }
}
