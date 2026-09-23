package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionDocumentoXsdService extends BaseService<VersionDocumentoXsd> {

	private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;

	@Override
	protected BaseRepository<VersionDocumentoXsd, Long> getRepository() {
		return versionDocumentoXsdRepository;
	}

	@Override
	@Transactional
	public VersionDocumentoXsd guardar(VersionDocumentoXsd entity) {
		validar(entity);
		boolean dup = entity.getId() == null ? versionDocumentoXsdRepository.existsByDocumentoXsdIdAndVersion(entity.getDocumentoXsd().getId(), entity.getVersion()) : versionDocumentoXsdRepository.existsByDocumentoXsdIdAndVersionAndIdNot(entity.getDocumentoXsd().getId(), entity.getVersion(), entity.getId());
		if (dup) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_DUPLICADA, entity.getVersion());
		return super.guardar(entity);
	}

	public List<VersionDocumentoXsd> listarPorDocumentoXsd(Long documentoXsdId) {
		return versionDocumentoXsdRepository.findByDocumentoXsdId(documentoXsdId).stream()
				.filter(version -> !EstadoRegistro.ELIMINADO.equals(version.getEstadoRegistro())).toList();
	}

	public Optional<VersionDocumentoXsd> obtenerPorDocumentoXsdYVersion(Long documentoXsdId, String version) {
		return versionDocumentoXsdRepository.findByDocumentoXsdIdAndVersion(documentoXsdId, version)
				.filter(versionDocumento -> EstadoRegistro.ACTIVO.equals(versionDocumento.getEstadoRegistro()));
	}

	public boolean existePorDocumentoXsdYVersion(Long documentoXsdId, String version) {
		return versionDocumentoXsdRepository.findByDocumentoXsdIdAndVersion(documentoXsdId, version)
				.filter(versionDocumento -> !EstadoRegistro.ELIMINADO.equals(versionDocumento.getEstadoRegistro()))
				.isPresent();
	}

	private void validar(VersionDocumentoXsd e) {
		if (e == null) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_REQUERIDA);
		if (e.getDocumentoXsd() == null || e.getDocumentoXsd().getId() == null) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_DOCUMENTO_REQUERIDO);
		if (e.getVersion() == null || e.getVersion().isBlank()) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_VERSION_REQUERIDA);
		if (e.getFechaInicio() == null) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_FECHA_INICIO_REQUERIDA);
		if (e.getFechaFin() != null && e.getFechaFin().isBefore(e.getFechaInicio())) throw new ApplicationException(MessageCodes.VERSION_DOCUMENTO_XSD_RANGO_FECHAS_INVALIDO);
	}

	public Optional<VersionDocumentoXsd> obtenerVigente(Long documentoXsdId, LocalDateTime fecha) {
		Optional<VersionDocumentoXsd> version = versionDocumentoXsdRepository
				.findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
						documentoXsdId, EstadoRegistro.ACTIVO, fecha, fecha);

		if (version.isPresent()) {
			return version;
		}

		return versionDocumentoXsdRepository
				.findByDocumentoXsdIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(documentoXsdId,
						EstadoRegistro.ACTIVO, fecha);
	}
}