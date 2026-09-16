package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-16T18:22:09-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class EmpresaMapperImpl implements EmpresaMapper {

    @Override
    public Empresa toEntity(EmpresaRequest request) {
        if ( request == null ) {
            return null;
        }

        Empresa.EmpresaBuilder empresa = Empresa.builder();

        empresa.ruc( request.getRuc() );
        empresa.razonSocial( request.getRazonSocial() );
        empresa.nombreComercial( request.getNombreComercial() );
        empresa.direccionMatriz( request.getDireccionMatriz() );
        empresa.obligadoContabilidad( request.getObligadoContabilidad() );
        empresa.contribuyenteRimpe( request.getContribuyenteRimpe() );

        return empresa.build();
    }

    @Override
    public EmpresaResponse toResponse(Empresa entity) {
        if ( entity == null ) {
            return null;
        }

        EmpresaResponse.EmpresaResponseBuilder empresaResponse = EmpresaResponse.builder();

        empresaResponse.id( entity.getId() );
        empresaResponse.ruc( entity.getRuc() );
        empresaResponse.razonSocial( entity.getRazonSocial() );
        empresaResponse.nombreComercial( entity.getNombreComercial() );
        empresaResponse.direccionMatriz( entity.getDireccionMatriz() );
        empresaResponse.obligadoContabilidad( entity.getObligadoContabilidad() );
        empresaResponse.contribuyenteRimpe( entity.getContribuyenteRimpe() );
        empresaResponse.estadoRegistro( entity.getEstadoRegistro() );
        empresaResponse.usuarioCreacion( entity.getUsuarioCreacion() );
        empresaResponse.usuarioModificacion( entity.getUsuarioModificacion() );
        empresaResponse.fechaCreacion( entity.getFechaCreacion() );
        empresaResponse.fechaModificacion( entity.getFechaModificacion() );
        empresaResponse.observacion( entity.getObservacion() );

        return empresaResponse.build();
    }
}
