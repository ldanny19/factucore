package ec.dalara.factucore.infrastructure.mapper.entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ec.dalara.factucore.domain.comprobante.ComprobanteModel;
import ec.dalara.factucore.domain.comprobante.ClaveAcceso;
import ec.dalara.factucore.domain.comprobante.NumeroComprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
@Mapper(config=ec.dalara.factucore.infrastructure.mapper.MapStructInfrastructureConfig.class)
public interface ComprobanteEntityMapper {
 default ComprobanteModel toModel(Comprobante e){if(e==null)return null;ComprobanteModel m=new ComprobanteModel(e.getId(),e.getEmpresa().getId(),e.getEstablecimiento().getId(),e.getPuntoEmision().getId(),e.getCodigoDocumento(),new NumeroComprobante(e.getEstablecimiento().getCodigo()+"-"+e.getPuntoEmision().getCodigo()+"-"+e.getSecuencial()),e.getFechaEmision());if(e.getEstadoProceso()!=null)m.cambiarEstadoProceso(e.getEstadoProceso());if(e.getClaveAcceso()!=null)m.asignarClaveAcceso(new ClaveAcceso(e.getClaveAcceso()));return m;}
 @Mapping(target="id",ignore=true)
 @Mapping(target="empresa",ignore=true)
 @Mapping(target="establecimiento",ignore=true)
 @Mapping(target="puntoEmision",ignore=true)
 @Mapping(target="documentoXsd",ignore=true)
 @Mapping(target="versionDocumentoXsd",ignore=true)
 @Mapping(target="datosComprobante",ignore=true)
 Comprobante toEntity(ComprobanteModel model);
}