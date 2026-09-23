package ec.dalara.factucore.application.mapper;

import org.mapstruct.Mapper;

import ec.dalara.factucore.application.contract.request.ElementoXsdRequest;
import ec.dalara.factucore.application.contract.response.ElementoXsdResponse;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;

@Mapper(config = MapStructApplicationConfig.class)
public interface ElementoXsdMapper {

    default ElementoXsdModel toModel(ElementoXsdRequest r) {
        if (r == null) {
            return null;
        }

        return new ElementoXsdModel(
                null,
                r.getIdVersionDocumentoXsd(),
                r.getIdElementoPadre(),
                r.getNombre(),
                r.getTipoDato(),
                r.getOrden(),
                r.getObligatorio(),
                r.getRepetible(),
                r.getMinOcurrencias(),
                r.getMaxOcurrencias(),
                r.getLongitudMinima(),
                r.getLongitudMaxima(),
                r.getDigitosTotales(),
                r.getDecimales(),
                r.getValorMinimo(),
                r.getValorMaximo(),
                r.getPatron(),
                r.getFechaInicio(),
                r.getFechaFin());
    }

    ElementoXsdResponse toResponse(ElementoXsdModel model);
}
