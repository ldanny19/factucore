package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-21T17:23:19-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ConfiguracionEmpresaMapperImpl implements ConfiguracionEmpresaMapper {

    @Override
    public ConfiguracionEmpresa toEntity(ConfiguracionEmpresaRequest request) {
        if ( request == null ) {
            return null;
        }

        ConfiguracionEmpresa.ConfiguracionEmpresaBuilder configuracionEmpresa = ConfiguracionEmpresa.builder();

        configuracionEmpresa.empresa( mapEmpresa( request.getIdEmpresa() ) );
        configuracionEmpresa.clave( request.getClave() );
        configuracionEmpresa.valor( request.getValor() );
        configuracionEmpresa.tipoDato( request.getTipoDato() );
        configuracionEmpresa.fechaVigenciaDesde( request.getFechaVigenciaDesde() );
        configuracionEmpresa.fechaVigenciaHasta( request.getFechaVigenciaHasta() );

        return configuracionEmpresa.build();
    }

    @Override
    public ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresa entity) {
        if ( entity == null ) {
            return null;
        }

        ConfiguracionEmpresaResponse.ConfiguracionEmpresaResponseBuilder configuracionEmpresaResponse = ConfiguracionEmpresaResponse.builder();

        configuracionEmpresaResponse.idEmpresa( entityEmpresaId( entity ) );
        configuracionEmpresaResponse.id( entity.getId() );
        configuracionEmpresaResponse.clave( entity.getClave() );
        configuracionEmpresaResponse.valor( entity.getValor() );
        configuracionEmpresaResponse.tipoDato( entity.getTipoDato() );
        configuracionEmpresaResponse.fechaVigenciaDesde( entity.getFechaVigenciaDesde() );
        configuracionEmpresaResponse.fechaVigenciaHasta( entity.getFechaVigenciaHasta() );
        configuracionEmpresaResponse.estadoRegistro( entity.getEstadoRegistro() );
        configuracionEmpresaResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        configuracionEmpresaResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        configuracionEmpresaResponse.fechaCreacion( entity.getFechaCreacion() );
        configuracionEmpresaResponse.fechaModificacion( entity.getFechaModificacion() );
        configuracionEmpresaResponse.observacion( entity.getObservacion() );

        return configuracionEmpresaResponse.build();
    }

    private Long entityEmpresaId(ConfiguracionEmpresa configuracionEmpresa) {
        Empresa empresa = configuracionEmpresa.getEmpresa();
        if ( empresa == null ) {
            return null;
        }
        return empresa.getId();
    }
}
