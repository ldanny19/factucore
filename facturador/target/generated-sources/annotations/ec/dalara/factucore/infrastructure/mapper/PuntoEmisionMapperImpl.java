package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class PuntoEmisionMapperImpl implements PuntoEmisionMapper {

    @Override
    public PuntoEmision toEntity(PuntoEmisionRequest request) {
        if ( request == null ) {
            return null;
        }

        PuntoEmision.PuntoEmisionBuilder puntoEmision = PuntoEmision.builder();

        puntoEmision.establecimiento( mapEstablecimiento( request.getIdEstablecimiento() ) );
        puntoEmision.codigo( request.getCodigo() );
        puntoEmision.nombre( request.getNombre() );

        return puntoEmision.build();
    }

    @Override
    public PuntoEmisionResponse toResponse(PuntoEmision entity) {
        if ( entity == null ) {
            return null;
        }

        PuntoEmisionResponse.PuntoEmisionResponseBuilder puntoEmisionResponse = PuntoEmisionResponse.builder();

        puntoEmisionResponse.idEstablecimiento( entityEstablecimientoId( entity ) );
        puntoEmisionResponse.id( entity.getId() );
        puntoEmisionResponse.codigo( entity.getCodigo() );
        puntoEmisionResponse.nombre( entity.getNombre() );
        puntoEmisionResponse.estadoRegistro( entity.getEstadoRegistro() );
        puntoEmisionResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        puntoEmisionResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        puntoEmisionResponse.fechaCreacion( entity.getFechaCreacion() );
        puntoEmisionResponse.fechaModificacion( entity.getFechaModificacion() );
        puntoEmisionResponse.observacion( entity.getObservacion() );

        return puntoEmisionResponse.build();
    }

    private Long entityEstablecimientoId(PuntoEmision puntoEmision) {
        Establecimiento establecimiento = puntoEmision.getEstablecimiento();
        if ( establecimiento == null ) {
            return null;
        }
        return establecimiento.getId();
    }
}
