package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface PuntoEmisionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establecimiento", source = "idEstablecimiento")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    PuntoEmision toEntity(PuntoEmisionRequest request);

    @Mapping(target = "idEstablecimiento", source = "establecimiento.id")
    PuntoEmisionResponse toResponse(PuntoEmision entity);

    default Establecimiento mapEstablecimiento(Long idEstablecimiento) {
        if (idEstablecimiento == null) {
            return null;
        }

        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setId(idEstablecimiento);
        return establecimiento;
    }
}