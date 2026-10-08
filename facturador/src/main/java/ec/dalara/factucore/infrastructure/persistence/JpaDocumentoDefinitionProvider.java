package ec.dalara.factucore.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.MapeoXsd;
import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.DocumentoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.MapeoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.VersionDocumentoXsdRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaDocumentoDefinitionProvider implements DocumentoDefinitionProvider {

	private final DocumentoXsdRepository documentoXsdRepository;
	private final VersionDocumentoXsdRepository versionDocumentoXsdRepository;
	private final ElementoXsdRepository elementoXsdRepository;
	private final AtributoXsdRepository atributoXsdRepository;
	private final EnumeracionXsdRepository enumeracionXsdRepository;
	private final MapeoXsdRepository mapeoXsdRepository;

	@Override
	@Transactional(readOnly = true)
	public Optional<VersionDocumentoXsdModel> obtenerVersion(Long idDocumentoXsd, String versionXsd) {
		return obtenerEntidadVersion(idDocumentoXsd, versionXsd).map(this::crearVersionModel);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<DocumentDefinitionModel> obtenerDefinicion(Long idDocumentoXsd, String versionXsd) {
		Optional<VersionDocumentoXsd> versionOptional = obtenerEntidadVersion(idDocumentoXsd, versionXsd);

		if (versionOptional.isEmpty()) {
			return Optional.empty();
		}

		VersionDocumentoXsd version = versionOptional.get();
		Long idVersionDocumentoXsd = version.getId();
		DocumentoXsd documento = version.getDocumentoXsd();

		List<ElementoXsd> elementos = elementoXsdRepository.findByVersionDocumentoXsdId(idVersionDocumentoXsd).stream()
				.filter(elemento -> EstadoRegistro.ACTIVO.equals(elemento.getEstadoRegistro())).toList();

		List<ElementoXsdModel> elementoModels = elementos.stream().map(this::crearElementoModel).toList();

		List<AtributoXsdModel> atributoModels = elementos.stream()
				.flatMap(elemento -> atributoXsdRepository.findByElementoXsdId(elemento.getId()).stream())
				.filter(atributo -> EstadoRegistro.ACTIVO.equals(atributo.getEstadoRegistro()))
				.map(this::crearAtributoModel).toList();

		List<EnumeracionXsdModel> enumeracionModels = elementos.stream()
				.flatMap(elemento -> enumeracionXsdRepository.findByElementoXsdId(elemento.getId()).stream())
				.filter(enumeracion -> EstadoRegistro.ACTIVO.equals(enumeracion.getEstadoRegistro()))
				.map(this::crearEnumeracionModel).toList();

		List<MapeoXsd> mapeos = mapeoXsdRepository
				.findByVersionDocumentoXsdIdAndEstadoRegistro(idVersionDocumentoXsd, EstadoRegistro.ACTIVO);

		List<MapeoXsdModel> mapeoModels = mapeos.stream()
				.map(this::crearMapeoModel).toList();

		DocumentoXsdModel documentoModel = new DocumentoXsdModel(documento.getCodigo(), documento.getNombre(),
				documento.getDescripcion(), documento.getTipoDocumento(), documento.getPrefijoArchivo());

		VersionDocumentoXsdModel versionModel = crearVersionModel(version);

		return Optional.of(new DocumentDefinitionModel(documentoModel, versionModel, elementoModels, atributoModels,
				enumeracionModels, mapeoModels));
	}

	private Optional<VersionDocumentoXsd> obtenerEntidadVersion(Long idDocumentoXsd, String versionXsd) {
		if (idDocumentoXsd == null || versionXsd == null || versionXsd.isBlank()) {
			return Optional.empty();
		}

		Optional<DocumentoXsd> documentoOptional = documentoXsdRepository.findById(idDocumentoXsd)
				.filter(documento -> EstadoRegistro.ACTIVO.equals(documento.getEstadoRegistro()));

		if (documentoOptional.isEmpty()) {
			return Optional.empty();
		}

		Optional<VersionDocumentoXsd> versionOptional = versionDocumentoXsdRepository
				.findByDocumentoXsdIdAndVersionAndEstadoRegistro(idDocumentoXsd, versionXsd, EstadoRegistro.ACTIVO);

		return versionOptional;
	}

	private VersionDocumentoXsdModel crearVersionModel(VersionDocumentoXsd version) {
		return new VersionDocumentoXsdModel(version.getId(), version.getDocumentoXsd().getId(), version.getVersion(),
				version.getNombreArchivo(), version.getNamespaceXml(), version.getElementoRaiz(),
				version.getPlantillaJson(), version.getEsquemaJson(), version.getFechaInicio(), version.getFechaFin());
	}

	private ElementoXsdModel crearElementoModel(ElementoXsd elemento) {
		return new ElementoXsdModel(elemento.getId(), elemento.getVersionDocumentoXsd().getId(),
				elemento.getElementoPadre() == null ? null : elemento.getElementoPadre().getId(), elemento.getNombre(),
				elemento.getTipoDato(), elemento.getOrden(), elemento.getObligatorio(), elemento.getRepetible(),
				elemento.getMinOcurrencias(), elemento.getMaxOcurrencias(), elemento.getLongitudMinima(),
				elemento.getLongitudMaxima(), elemento.getDigitosTotales(), elemento.getDecimales(),
				elemento.getValorMinimo(), elemento.getValorMaximo(), elemento.getPatron(), elemento.getFechaInicio(),
				elemento.getFechaFin());
	}

	private AtributoXsdModel crearAtributoModel(AtributoXsd atributo) {
		return new AtributoXsdModel(atributo.getId(), atributo.getElementoXsd().getId(), atributo.getNombre(),
				atributo.getTipoDato(), atributo.getObligatorio(), atributo.getValorPredeterminado(),
				atributo.getPatron());
	}

	private EnumeracionXsdModel crearEnumeracionModel(EnumeracionXsd enumeracion) {
		return new EnumeracionXsdModel(enumeracion.getId(), enumeracion.getElementoXsd().getId(),
				enumeracion.getValor(), enumeracion.getDescripcion(), enumeracion.getOrden());
	}

	private MapeoXsdModel crearMapeoModel(MapeoXsd mapeo) {
		return new MapeoXsdModel(mapeo.getId(), mapeo.getVersionDocumentoXsd().getId(), mapeo.getTipoOrigen(),
				mapeo.getOrigen(), mapeo.getElementoXsd() == null ? null : mapeo.getElementoXsd().getId(),
				mapeo.getAtributoXsd() == null ? null : mapeo.getAtributoXsd().getId(), mapeo.getTipoMapeo());
	}
}
