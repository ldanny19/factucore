package ec.dalara.factucore.infrastructure.mapper;

import ec.dalara.factucore.application.contract.request.AtributoXsdRequest;
import ec.dalara.factucore.application.contract.response.AtributoXsdResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AtributoXsdMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "elementoXsd", source = "idElementoXsd")
    @Mapping(target = "estadoRegistro", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "observacion", ignore = true)
    AtributoXsd toEntity(AtributoXsdRequest request);

    @Mapping(target = "idElementoXsd", source = "elementoXsd.id")
    AtributoXsdResponse toResponse(AtributoXsd entity);

    default ElementoXsd mapElementoXsd(Long idElementoXsd) {

        if (idElementoXsd == null) {
            return null;
        }

        ElementoXsd elemento = new ElementoXsd();
        elemento.setId(idElementoXsd);

        return elemento;
    }
}