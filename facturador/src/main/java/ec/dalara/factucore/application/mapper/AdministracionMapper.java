package ec.dalara.factucore.application.mapper;

import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.request.VersionDocumentoXsdRequest;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;

public final class AdministracionMapper {
	private AdministracionMapper() {
	}

	public static Empresa toEntity(EmpresaRequest r, Empresa e) {
		if (e == null)
			e = new Empresa();
		e.setRuc(r.getRuc());
		e.setRazonSocial(r.getRazonSocial());
		e.setNombreComercial(r.getNombreComercial());
		e.setDireccionMatriz(r.getDireccionMatriz());
		e.setObligadoContabilidad(r.getObligadoContabilidad());
		e.setContribuyenteRimpe(r.getContribuyenteRimpe());
		return e;
	}

	public static EmpresaResponse toResponse(Empresa e) {
		return EmpresaResponse.builder().id(e.getId()).ruc(e.getRuc()).razonSocial(e.getRazonSocial())
				.nombreComercial(e.getNombreComercial()).direccionMatriz(e.getDireccionMatriz())
				.obligadoContabilidad(e.getObligadoContabilidad()).contribuyenteRimpe(e.getContribuyenteRimpe())
				.estadoRegistro(e.getEstadoRegistro()).usuarioCreacion(e.getUsuarioCreacion())
				.usuarioModificacion(e.getUsuarioModificacion()).fechaCreacion(e.getFechaCreacion())
				.fechaModificacion(e.getFechaModificacion()).observacion(e.getObservacion()).build();
	}

	public static Establecimiento toEntity(EstablecimientoRequest r, Establecimiento e) {
		if (e == null)
			e = new Establecimiento();
		e.setCodigo(r.getCodigo());
		e.setNombre(r.getNombre());
		e.setDireccion(r.getDireccion());
		return e;
	}

	public static EstablecimientoResponse toResponse(Establecimiento e) {
		return EstablecimientoResponse.builder().id(e.getId()).idEmpresa(e.getEmpresa().getId()).codigo(e.getCodigo())
				.nombre(e.getNombre()).direccion(e.getDireccion()).estadoRegistro(e.getEstadoRegistro())
				.usuarioCreacion(e.getUsuarioCreacion()).usuarioModificacion(e.getUsuarioModificacion())
				.fechaCreacion(e.getFechaCreacion()).fechaModificacion(e.getFechaModificacion())
				.observacion(e.getObservacion()).build();
	}

	public static PuntoEmision toEntity(PuntoEmisionRequest r, PuntoEmision e) {
		if (e == null)
			e = new PuntoEmision();
		e.setCodigo(r.getCodigo());
		e.setNombre(r.getNombre());
		return e;
	}

	public static PuntoEmisionResponse toResponse(PuntoEmision e) {
		return PuntoEmisionResponse.builder().id(e.getId()).idEstablecimiento(e.getEstablecimiento().getId())
				.codigo(e.getCodigo()).nombre(e.getNombre()).estadoRegistro(e.getEstadoRegistro())
				.usuarioCreacion(e.getUsuarioCreacion()).usuarioModificacion(e.getUsuarioModificacion())
				.fechaCreacion(e.getFechaCreacion()).fechaModificacion(e.getFechaModificacion())
				.observacion(e.getObservacion()).build();
	}

	public static Secuencial toEntity(SecuencialRequest r, Secuencial e) {
		if (e == null)
			e = new Secuencial();
		e.setCodigoDocumento(r.getCodigoDocumento());
		e.setUltimoSecuencial(r.getUltimoSecuencial());
		return e;
	}

	public static SecuencialResponse toResponse(Secuencial e) {
		return SecuencialResponse.builder().id(e.getId()).idPuntoEmision(e.getPuntoEmision().getId())
				.codigoDocumento(e.getCodigoDocumento()).ultimoSecuencial(e.getUltimoSecuencial())
				.estadoRegistro(e.getEstadoRegistro()).usuarioCreacion(e.getUsuarioCreacion())
				.usuarioModificacion(e.getUsuarioModificacion()).fechaCreacion(e.getFechaCreacion())
				.fechaModificacion(e.getFechaModificacion()).observacion(e.getObservacion()).build();
	}

	public static CertificadoFirma toEntity(CertificadoFirmaRequest r, CertificadoFirma e) {
		if (e == null)
			e = new CertificadoFirma();
		e.setNombreArchivo(r.getNombreArchivo());
		e.setRutaCertificado(r.getRutaCertificado());
		e.setFechaInicio(r.getFechaInicio());
		e.setFechaFin(r.getFechaFin());
		return e;
	}

	public static CertificadoFirmaResponse toResponse(CertificadoFirma e) {
		return CertificadoFirmaResponse.builder().id(e.getId()).idEmpresa(e.getEmpresa().getId())
				.nombreArchivo(e.getNombreArchivo()).rutaCertificado(e.getRutaCertificado())
				.fechaInicio(e.getFechaInicio()).fechaFin(e.getFechaFin()).estadoRegistro(e.getEstadoRegistro())
				.usuarioCreacion(e.getUsuarioCreacion()).usuarioModificacion(e.getUsuarioModificacion())
				.fechaCreacion(e.getFechaCreacion()).fechaModificacion(e.getFechaModificacion())
				.observacion(e.getObservacion()).build();
	}

	public static ConfiguracionEmpresa toEntity(ConfiguracionEmpresaRequest r, ConfiguracionEmpresa e) {
		if (e == null)
			e = new ConfiguracionEmpresa();
		e.setClave(r.getClave());
		e.setValor(r.getValor());
		e.setTipoDato(r.getTipoDato());
		e.setFechaVigenciaDesde(r.getFechaVigenciaDesde());
		e.setFechaVigenciaHasta(r.getFechaVigenciaHasta());
		return e;
	}

	public static ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresa e) {
		return ConfiguracionEmpresaResponse.builder().id(e.getId()).idEmpresa(e.getEmpresa().getId())
				.clave(e.getClave()).valor(e.getValor()).tipoDato(e.getTipoDato())
				.fechaVigenciaDesde(e.getFechaVigenciaDesde()).fechaVigenciaHasta(e.getFechaVigenciaHasta())
				.estadoRegistro(e.getEstadoRegistro()).usuarioCreacion(e.getUsuarioCreacion())
				.usuarioModificacion(e.getUsuarioModificacion()).fechaCreacion(e.getFechaCreacion())
				.fechaModificacion(e.getFechaModificacion()).observacion(e.getObservacion()).build();
	}

	public static DocumentoXsd toEntity(DocumentoXsdRequest r, DocumentoXsd e) {
		if (e == null)
			e = new DocumentoXsd();
		e.setCodigo(r.getCodigo());
		e.setNombre(r.getNombre());
		e.setDescripcion(r.getDescripcion());
		e.setTipoDocumento(r.getTipoDocumento());
		e.setPrefijoArchivo(r.getPrefijoArchivo());
		return e;
	}

	public static VersionDocumentoXsd toEntity(VersionDocumentoXsdRequest r, VersionDocumentoXsd e) {
		if (e == null)
			e = new VersionDocumentoXsd();
		e.setVersion(r.getVersion());
		e.setNombreArchivo(r.getNombreArchivo());
		e.setNamespaceXml(r.getNamespaceXml());
		e.setElementoRaiz(r.getElementoRaiz());
		e.setPlantillaJson(r.getPlantillaJson());
		e.setEsquemaJson(r.getEsquemaJson());
		e.setFechaInicio(r.getFechaInicio());
		e.setFechaFin(r.getFechaFin());
		return e;
	}

	public static DocumentoXsdResponse toResponse(DocumentoXsd e) {
		return toResponse(e, null);
	}

	public static DocumentoXsdResponse toResponse(DocumentoXsd e, Long idVersionDocumentoXsd) {
		return DocumentoXsdResponse.builder().id(e.getId()).idVersionDocumentoXsd(idVersionDocumentoXsd)
				.codigo(e.getCodigo()).nombre(e.getNombre()).descripcion(e.getDescripcion())
				.tipoDocumento(e.getTipoDocumento()).prefijoArchivo(e.getPrefijoArchivo())
				.estadoRegistro(e.getEstadoRegistro()).usuarioCreacion(e.getUsuarioCreacion())
				.usuarioModificacion(e.getUsuarioModificacion()).fechaCreacion(e.getFechaCreacion())
				.fechaModificacion(e.getFechaModificacion()).observacion(e.getObservacion()).build();
	}
}
