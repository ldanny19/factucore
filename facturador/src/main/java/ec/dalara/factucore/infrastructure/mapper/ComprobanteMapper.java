package ec.dalara.factucore.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ec.dalara.factucore.application.contract.request.ComprobanteRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;

@Mapper(config = MapStructConfig.class)
public interface ComprobanteMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "empresa", source = "idEmpresa")
	@Mapping(target = "establecimiento", source = "idEstablecimiento")
	@Mapping(target = "puntoEmision", source = "idPuntoEmision")
	@Mapping(target = "documentoXsd", source = "idDocumentoXsd")
	@Mapping(target = "versionDocumentoXsd", source = "idVersionDocumentoXsd")
	@Mapping(target = "estadoRegistro", ignore = true)
	@Mapping(target = "usuarioCreacion", ignore = true)
	@Mapping(target = "usuarioModificacion", ignore = true)
	@Mapping(target = "fechaCreacion", ignore = true)
	@Mapping(target = "fechaModificacion", ignore = true)
	@Mapping(target = "observacion", ignore = true)
	Comprobante toEntity(ComprobanteRequest request);

	@Mapping(target = "idEmpresa", source = "empresa.id")
	@Mapping(target = "idEstablecimiento", source = "establecimiento.id")
	@Mapping(target = "idPuntoEmision", source = "puntoEmision.id")
	@Mapping(target = "idDocumentoXsd", source = "documentoXsd.id")
	@Mapping(target = "idVersionDocumentoXsd", source = "versionDocumentoXsd.id")
	ComprobanteResponse toResponse(Comprobante entity);

	default Empresa mapEmpresa(Long idEmpresa) {

		if (idEmpresa == null) {
			return null;
		}

		Empresa empresa = new Empresa();
		empresa.setId(idEmpresa);

		return empresa;
	}

	default Establecimiento mapEstablecimiento(Long idEstablecimiento) {

		if (idEstablecimiento == null) {
			return null;
		}

		Establecimiento establecimiento = new Establecimiento();
		establecimiento.setId(idEstablecimiento);

		return establecimiento;
	}

	default PuntoEmision mapPuntoEmision(Long idPuntoEmision) {

		if (idPuntoEmision == null) {
			return null;
		}

		PuntoEmision puntoEmision = new PuntoEmision();
		puntoEmision.setId(idPuntoEmision);

		return puntoEmision;
	}

	default DocumentoXsd mapDocumentoXsd(Long idDocumentoXsd) {

		if (idDocumentoXsd == null) {
			return null;
		}

		DocumentoXsd documentoXsd = new DocumentoXsd();
		documentoXsd.setId(idDocumentoXsd);

		return documentoXsd;
	}

	default VersionDocumentoXsd mapVersionDocumentoXsd(Long idVersionDocumentoXsd) {

		if (idVersionDocumentoXsd == null) {
			return null;
		}

		VersionDocumentoXsd version = new VersionDocumentoXsd();
		version.setId(idVersionDocumentoXsd);

		return version;
	}
}