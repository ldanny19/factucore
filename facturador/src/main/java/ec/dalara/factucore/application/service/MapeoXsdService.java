package ec.dalara.factucore.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.contract.request.MapeoXsdRequest;
import ec.dalara.factucore.application.contract.response.MapeoXsdResponse;
import ec.dalara.factucore.domain.shared.DomainException;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.application.mapper.MapeoXsdMapper;
import ec.dalara.factucore.infrastructure.mapper.entity.MapeoXsdEntityMapper;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.MapeoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapeoXsdService extends BaseService<MapeoXsd> {

	private final MapeoXsdRepository mapeoXsdRepository;
	private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;
	private final ElementoXsdRepository elementoXsdRepository;
	private final AtributoXsdRepository atributoXsdRepository;
	private final MapeoXsdMapper mapeoXsdMapper;
	private final MapeoXsdEntityMapper mapeoXsdEntityMapper;

	@Override
	protected BaseRepository<MapeoXsd, Long> getRepository() {
		return mapeoXsdRepository;
	}

	@Transactional
	public MapeoXsdResponse crear(MapeoXsdRequest request) {
		validarDestino(request);

		VersionDocumentoXsd version = versionDocumentoXsdRepository
				.findByIdAndEstadoRegistro(request.getIdVersionDocumentoXsd(), EstadoRegistro.ACTIVO)
				.orElseThrow(() -> new DomainException(MessageCodes.MAPEO_XSD_VERSION_NO_ENCONTRADA));

		ElementoXsd elemento = null;
		if (request.getIdElementoXsd() != null) {
			elemento = elementoXsdRepository
					.findByIdAndEstadoRegistro(request.getIdElementoXsd(), EstadoRegistro.ACTIVO)
					.orElseThrow(() -> new DomainException(MessageCodes.MAPEO_XSD_ELEMENTO_NO_ENCONTRADO));

			if (elemento.getVersionDocumentoXsd() == null
					|| !request.getIdVersionDocumentoXsd().equals(elemento.getVersionDocumentoXsd().getId())) {
				throw new DomainException(MessageCodes.MAPEO_XSD_ELEMENTO_VERSION_INVALIDA);
			}
		}

		AtributoXsd atributo = null;
		if (request.getIdAtributoXsd() != null) {
			atributo = atributoXsdRepository
					.findByIdAndEstadoRegistro(request.getIdAtributoXsd(), EstadoRegistro.ACTIVO)
					.orElseThrow(() -> new DomainException(MessageCodes.MAPEO_XSD_ATRIBUTO_NO_ENCONTRADO));

			if (atributo.getElementoXsd() == null || atributo.getElementoXsd().getVersionDocumentoXsd() == null
					|| !request.getIdVersionDocumentoXsd()
							.equals(atributo.getElementoXsd().getVersionDocumentoXsd().getId())) {
				throw new DomainException(MessageCodes.MAPEO_XSD_ATRIBUTO_VERSION_INVALIDA);
			}
		}

		if (mapeoXsdRepository.existsByVersionDocumentoXsdIdAndRutaOrigen(request.getIdVersionDocumentoXsd(),
				request.getRutaOrigen())) {
			throw new DomainException(MessageCodes.MAPEO_XSD_RUTA_ORIGEN_DUPLICADA);
		}

		MapeoXsd entity = mapeoXsdEntityMapper.toEntity(mapeoXsdMapper.toModel(request));
		entity.setVersionDocumentoXsd(version);
		entity.setElementoXsd(elemento);
		entity.setAtributoXsd(atributo);

		MapeoXsd guardado = guardar(entity);
		return mapeoXsdMapper.toResponse(guardado);
	}

	@Transactional(readOnly = true)
	public List<MapeoXsdResponse> listarPorVersion(Long versionDocumentoXsdId) {
		return mapeoXsdRepository
				.findByVersionDocumentoXsdIdAndEstadoRegistro(versionDocumentoXsdId, EstadoRegistro.ACTIVO).stream()
				.map(mapeoXsdMapper::toResponse).toList();
	}

	private void validarDestino(MapeoXsdRequest request) {
		boolean elemento = request.getIdElementoXsd() != null;
		boolean atributo = request.getIdAtributoXsd() != null;

		if (elemento == atributo) {
			throw new DomainException(MessageCodes.MAPEO_XSD_DESTINO_INVALIDO);
		}

		if ("ELEMENTO".equals(request.getTipoMapeo()) && !elemento) {
			throw new DomainException(MessageCodes.MAPEO_XSD_TIPO_DESTINO_INVALIDO);
		}

		if ("ATRIBUTO".equals(request.getTipoMapeo()) && !atributo) {
			throw new DomainException(MessageCodes.MAPEO_XSD_TIPO_DESTINO_INVALIDO);
		}
	}
}
