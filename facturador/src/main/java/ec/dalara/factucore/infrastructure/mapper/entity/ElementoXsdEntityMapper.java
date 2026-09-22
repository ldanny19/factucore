package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ElementoXsdEntityMapper {
 default ElementoXsdModel toModel(ElementoXsd e){if(e==null)return null;return new ElementoXsdModel(e.getId(),e.getVersionDocumentoXsd().getId(),e.getElementoPadre()==null?null:e.getElementoPadre().getId(),e.getNombre(),e.getTipoDato(),e.getOrden(),e.getObligatorio(),e.getRepetible(),e.getMinOcurrencias(),e.getMaxOcurrencias(),e.getLongitudMinima(),e.getLongitudMaxima(),e.getDigitosTotales(),e.getDecimales(),e.getValorMinimo(),e.getValorMaximo(),e.getPatron());}
 @Mapping(target="id",ignore=true)
 @Mapping(target="versionDocumentoXsd", ignore=true)
    @Mapping(target="elementoPadre", ignore=true)
 ElementoXsd toEntity(ElementoXsdModel model);
}