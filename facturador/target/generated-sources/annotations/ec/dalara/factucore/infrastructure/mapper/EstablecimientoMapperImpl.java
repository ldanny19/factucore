package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class EstablecimientoMapperImpl implements EstablecimientoMapper {

    @Override
    public Establecimiento toEntity(EstablecimientoRequest request) {
        if ( request == null ) {
            return null;
        }

        Establecimiento.EstablecimientoBuilder establecimiento = Establecimiento.builder();

        establecimiento.empresa( mapEmpresa( request.getIdEmpresa() ) );
        establecimiento.codigo( request.getCodigo() );
        establecimiento.nombre( request.getNombre() );
        establecimiento.direccion( request.getDireccion() );

        return establecimiento.build();
    }

    @Override
    public EstablecimientoResponse toResponse(Establecimiento entity) {
        if ( entity == null ) {
            return null;
        }

        EstablecimientoResponse.EstablecimientoResponseBuilder establecimientoResponse = EstablecimientoResponse.builder();

        establecimientoResponse.idEmpresa( entityEmpresaId( entity ) );
        establecimientoResponse.id( entity.getId() );
        establecimientoResponse.codigo( entity.getCodigo() );
        establecimientoResponse.nombre( entity.getNombre() );
        establecimientoResponse.direccion( entity.getDireccion() );
        establecimientoResponse.estadoRegistro( entity.getEstadoRegistro() );
        establecimientoResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        establecimientoResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        establecimientoResponse.fechaCreacion( entity.getFechaCreacion() );
        establecimientoResponse.fechaModificacion( entity.getFechaModificacion() );
        establecimientoResponse.observacion( entity.getObservacion() );

        return establecimientoResponse.build();
    }

    private Long entityEmpresaId(Establecimiento establecimiento) {
        Empresa empresa = establecimiento.getEmpresa();
        if ( empresa == null ) {
            return null;
        }
        return empresa.getId();
    }
}
