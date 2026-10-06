package ec.dalara.factucore.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.CertificadoFirmaRequest;
import ec.dalara.factucore.application.contract.request.ConfiguracionEmpresaRequest;
import ec.dalara.factucore.application.contract.request.DocumentoXsdRequest;
import ec.dalara.factucore.application.contract.request.EmpresaRequest;
import ec.dalara.factucore.application.contract.request.EstablecimientoRequest;
import ec.dalara.factucore.application.contract.request.PuntoEmisionRequest;
import ec.dalara.factucore.application.contract.request.SecuencialRequest;
import ec.dalara.factucore.application.contract.response.CertificadoFirmaResponse;
import ec.dalara.factucore.application.contract.response.ConfiguracionEmpresaResponse;
import ec.dalara.factucore.application.contract.response.DocumentoXsdResponse;
import ec.dalara.factucore.application.contract.response.EmpresaResponse;
import ec.dalara.factucore.application.contract.response.EstablecimientoResponse;
import ec.dalara.factucore.application.contract.response.PuntoEmisionResponse;
import ec.dalara.factucore.application.contract.response.SecuencialResponse;
import ec.dalara.factucore.application.mapper.AdministracionMapper;
import ec.dalara.factucore.application.port.in.AdministracionPort;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportRequest;
import ec.dalara.factucore.domain.documentoxsd.importacion.XsdImportResult;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import ec.dalara.factucore.infrastructure.persistence.repository.EmpresaRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EstablecimientoRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.PuntoEmisionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdministracionApplicationService implements AdministracionPort {
	private final EmpresaService empresaService;
	private final EstablecimientoService establecimientoService;
	private final PuntoEmisionService puntoEmisionService;
	private final SecuencialService secuencialService;
	private final ConfiguracionEmpresaService configuracionEmpresaService;
	private final CertificadoFirmaService certificadoFirmaService;
	private final DocumentoXsdService documentoXsdService;
	private final XsdImportService xsdImportService;

	@Value("${factucore.path.documentos}")
	private String rutaDocumentos;
	private final EmpresaRepository empresaRepository;
	private final EstablecimientoRepository establecimientoRepository;
	private final PuntoEmisionRepository puntoEmisionRepository;

	public EmpresaResponse guardarEmpresa(Long id, EmpresaRequest r) {
		Empresa e = id == null ? null : empresaService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		return AdministracionMapper.toResponse(empresaService.guardar(AdministracionMapper.toEntity(r, e)));
	}

	public EmpresaResponse obtenerEmpresa(Long id) {
		return empresaService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<EmpresaResponse> listarEmpresas() {
		return empresaService.listar().stream().map(AdministracionMapper::toResponse).toList();
	}

	public void inactivarEmpresa(Long id) {
		empresaService.inactivar(id);
	}

	public void reactivarEmpresa(Long id) {
		empresaService.reactivar(id);
	}

	public void eliminarEmpresa(Long id) {
		empresaService.eliminar(id);
	}

	public EstablecimientoResponse guardarEstablecimiento(Long id, EstablecimientoRequest r) {
		Establecimiento e = id == null ? null
				: establecimientoService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		e = AdministracionMapper.toEntity(r, e);
		e.setEmpresa(empresaRepository.findById(r.getIdEmpresa()).orElseThrow(() -> noEncontrado(r.getIdEmpresa())));
		return AdministracionMapper.toResponse(establecimientoService.guardar(e));
	}

	public EstablecimientoResponse obtenerEstablecimiento(Long id) {
		return establecimientoService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<EstablecimientoResponse> listarEstablecimientos(Long idEmpresa) {
		return establecimientoService.listarPorEmpresa(idEmpresa).stream().map(AdministracionMapper::toResponse)
				.toList();
	}

	public void inactivarEstablecimiento(Long id) {
		establecimientoService.inactivar(id);
	}

	public void reactivarEstablecimiento(Long id) {
		establecimientoService.reactivar(id);
	}

	public void eliminarEstablecimiento(Long id) {
		establecimientoService.eliminar(id);
	}

	public PuntoEmisionResponse guardarPuntoEmision(Long id, PuntoEmisionRequest r) {
		PuntoEmision e = id == null ? null : puntoEmisionService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		e = AdministracionMapper.toEntity(r, e);
		e.setEstablecimiento(establecimientoRepository.findById(r.getIdEstablecimiento())
				.orElseThrow(() -> noEncontrado(r.getIdEstablecimiento())));
		return AdministracionMapper.toResponse(puntoEmisionService.guardar(e));
	}

	public PuntoEmisionResponse obtenerPuntoEmision(Long id) {
		return puntoEmisionService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<PuntoEmisionResponse> listarPuntosEmision(Long id) {
		return puntoEmisionService.listarPorEstablecimiento(id).stream().map(AdministracionMapper::toResponse).toList();
	}

	public void inactivarPuntoEmision(Long id) {
		puntoEmisionService.inactivar(id);
	}

	public void reactivarPuntoEmision(Long id) {
		puntoEmisionService.reactivar(id);
	}

	public void eliminarPuntoEmision(Long id) {
		puntoEmisionService.eliminar(id);
	}

	public SecuencialResponse guardarSecuencial(Long id, SecuencialRequest r) {
		Secuencial e = id == null ? null : secuencialService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		e = AdministracionMapper.toEntity(r, e);
		e.setPuntoEmision(puntoEmisionRepository.findById(r.getIdPuntoEmision())
				.orElseThrow(() -> noEncontrado(r.getIdPuntoEmision())));
		return AdministracionMapper.toResponse(secuencialService.guardar(e));
	}

	public SecuencialResponse obtenerSecuencial(Long id) {
		return secuencialService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<SecuencialResponse> listarSecuenciales() {
		return secuencialService.listar().stream().map(AdministracionMapper::toResponse).toList();
	}

	public void inactivarSecuencial(Long id) {
		secuencialService.inactivar(id);
	}

	public void reactivarSecuencial(Long id) {
		secuencialService.reactivar(id);
	}

	public void eliminarSecuencial(Long id) {
		secuencialService.eliminar(id);
	}

	public ConfiguracionEmpresaResponse guardarConfiguracion(Long id, ConfiguracionEmpresaRequest r) {
		ConfiguracionEmpresa e = id == null ? null
				: configuracionEmpresaService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		e = AdministracionMapper.toEntity(r, e);
		e.setEmpresa(empresaRepository.findById(r.getIdEmpresa()).orElseThrow(() -> noEncontrado(r.getIdEmpresa())));
		return AdministracionMapper.toResponse(configuracionEmpresaService.guardar(e));
	}

	public ConfiguracionEmpresaResponse obtenerConfiguracion(Long id) {
		return configuracionEmpresaService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<ConfiguracionEmpresaResponse> listarConfiguraciones(Long id) {
		return configuracionEmpresaService.listarPorEmpresa(id).stream().map(AdministracionMapper::toResponse).toList();
	}

	public void inactivarConfiguracion(Long id) {
		configuracionEmpresaService.inactivar(id);
	}

	public void reactivarConfiguracion(Long id) {
		configuracionEmpresaService.reactivar(id);
	}

	public void eliminarConfiguracion(Long id) {
		configuracionEmpresaService.eliminar(id);
	}

	public CertificadoFirmaResponse guardarCertificadoFirma(Long id, CertificadoFirmaRequest r) {
		CertificadoFirma e = id == null ? null
				: certificadoFirmaService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		e = AdministracionMapper.toEntity(r, e);
		e.setEmpresa(empresaRepository.findById(r.getIdEmpresa()).orElseThrow(() -> noEncontrado(r.getIdEmpresa())));
		return AdministracionMapper.toResponse(certificadoFirmaService.guardar(e));
	}

	public CertificadoFirmaResponse obtenerCertificadoFirma(Long id) {
		return certificadoFirmaService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<CertificadoFirmaResponse> listarCertificadosFirma(Long idEmpresa) {
		return certificadoFirmaService.listarPorEmpresa(idEmpresa).stream().map(AdministracionMapper::toResponse)
				.toList();
	}

	public void inactivarCertificadoFirma(Long id) {
		certificadoFirmaService.inactivar(id);
	}

	public void reactivarCertificadoFirma(Long id) {
		certificadoFirmaService.reactivar(id);
	}

	public void eliminarCertificadoFirma(Long id) {
		certificadoFirmaService.eliminar(id);
	}

	public DocumentoXsdResponse guardarDocumentoXsd(Long id, DocumentoXsdRequest r) {
		if (id != null)
			return guardarDocumentoXsdExistente(id, r);

		Path rutaXsd = validarRutaXsd(r);
		String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
		String nombreArchivo = r.getVersion().getNombreArchivo();
		if (nombreArchivo == null || nombreArchivo.isBlank()) {
			nombreArchivo = rutaXsd.getFileName().toString();
		}

		XsdImportResult resultado;
		try (InputStream inputStream = Files.newInputStream(rutaXsd)) {
			resultado = xsdImportService.importar(inputStream, rutaXsd.toUri().toString(),
					new XsdImportRequest(r.getCodigo(), r.getNombre(), r.getDescripcion(), r.getTipoDocumento(),
							r.getPrefijoArchivo(), r.getVersion().getVersion(), nombreArchivo, r.getVersion().getFechaInicio(),
							r.getVersion().getFechaFin(), usuario, null));
		} catch (IOException exception) {
			throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_RUTA_INVALIDA, exception,
					r.getVersion().getRutaXsd());
		}

		DocumentoXsd documento = documentoXsdService.obtenerPorCodigo(r.getCodigo())
				.orElseThrow(() -> new ApplicationException(MessageCodes.REGISTRO_NO_ENCONTRADO, r.getCodigo()));
		return AdministracionMapper.toResponse(documento, resultado.versionDocumentoXsdId());
	}

	private DocumentoXsdResponse guardarDocumentoXsdExistente(Long id, DocumentoXsdRequest r) {
		DocumentoXsd e = documentoXsdService.obtenerPorId(id).orElseThrow(() -> noEncontrado(id));
		return AdministracionMapper.toResponse(documentoXsdService.guardar(AdministracionMapper.toEntity(r, e)));
	}

	private Path validarRutaXsd(DocumentoXsdRequest r) {
		String rutaSolicitada = r.getVersion().getRutaXsd();
		Path base = Path.of(rutaDocumentos).toAbsolutePath().normalize();
		Path ruta = Path.of(rutaSolicitada);
		if (!ruta.isAbsolute())
			ruta = base.resolve(ruta).normalize();
		else
			ruta = ruta.toAbsolutePath().normalize();

		if (!ruta.startsWith(base) || !Files.isRegularFile(ruta))
			throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_RUTA_INVALIDA, rutaSolicitada);

		return ruta;
	}

	public DocumentoXsdResponse obtenerDocumentoXsd(Long id) {
		return documentoXsdService.obtenerPorId(id).map(AdministracionMapper::toResponse)
				.orElseThrow(() -> noEncontrado(id));
	}

	public List<DocumentoXsdResponse> listarDocumentosXsd() {
		return documentoXsdService.listar().stream().map(AdministracionMapper::toResponse).toList();
	}

	public void inactivarDocumentoXsd(Long id) {
		documentoXsdService.inactivar(id);
	}

	public void reactivarDocumentoXsd(Long id) {
		documentoXsdService.reactivar(id);
	}

	public void eliminarDocumentoXsd(Long id) {
		documentoXsdService.eliminar(id);
	}

	private ApplicationException noEncontrado(Long id) {
		return new ApplicationException(MessageCodes.REGISTRO_NO_ENCONTRADO, id);
	}
}
