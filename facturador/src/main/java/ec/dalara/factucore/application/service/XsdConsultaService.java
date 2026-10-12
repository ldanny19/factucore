package ec.dalara.factucore.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.VersionDocumentoXsdModel;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
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

@Service
@RequiredArgsConstructor
public class XsdConsultaService {

	private final DocumentoXsdRepository documentoRepository;
	private final VersionDocumentoXsdRepository versionRepository;
	private final ElementoXsdRepository elementoRepository;
	private final AtributoXsdRepository atributoRepository;
	private final EnumeracionXsdRepository enumeracionRepository;
	private final MapeoXsdRepository mapeoRepository;

	@Transactional(readOnly = true)
	public List<VersionDocumentoXsdModel> listarVersiones(Long documentoXsdId) {
		if (documentoRepository.findByIdAndEstadoRegistro(documentoXsdId, EstadoRegistro.ACTIVO).isEmpty()) {
			throw new ApplicationException(MessageCodes.REGISTRO_NO_ENCONTRADO, documentoXsdId);
		}

		return versionRepository.findByDocumentoXsdId(documentoXsdId).stream()
				.filter(version -> EstadoRegistro.ACTIVO.equals(version.getEstadoRegistro()))
				.map(this::crearVersionModel).toList();
	}

	@Transactional(readOnly = true)
	public DocumentDefinitionModel obtenerDefinicion(Long versionDocumentoXsdId) {
		VersionDocumentoXsd version = obtenerVersion(versionDocumentoXsdId);
		DocumentoXsd documento = version.getDocumentoXsd();

		List<ElementoXsd> elementos = elementoRepository.findByVersionDocumentoXsdId(version.getId()).stream()
				.filter(elemento -> EstadoRegistro.ACTIVO.equals(elemento.getEstadoRegistro())).toList();

		List<ElementoXsdModel> elementoModels = elementos.stream().map(this::crearElementoModel).toList();

		List<AtributoXsdModel> atributoModels = elementos.stream()
				.flatMap(elemento -> atributoRepository.findByElementoXsdId(elemento.getId()).stream())
				.filter(atributo -> EstadoRegistro.ACTIVO.equals(atributo.getEstadoRegistro()))
				.map(this::crearAtributoModel).toList();

		List<EnumeracionXsdModel> enumeracionModels = elementos.stream()
				.flatMap(elemento -> enumeracionRepository.findByElementoXsdId(elemento.getId()).stream())
				.filter(enumeracion -> EstadoRegistro.ACTIVO.equals(enumeracion.getEstadoRegistro()))
				.map(this::crearEnumeracionModel).toList();

		List<MapeoXsdModel> mapeoModels = mapeoRepository
				.findByVersionDocumentoXsdIdAndEstadoRegistro(version.getId(), EstadoRegistro.ACTIVO).stream()
				.map(this::crearMapeoModel).toList();

		DocumentoXsdModel documentoModel = new DocumentoXsdModel(documento.getCodigo(), documento.getNombre(),
				documento.getDescripcion(), documento.getTipoDocumento(), documento.getPrefijoArchivo());

		return new DocumentDefinitionModel(documentoModel, crearVersionModel(version), elementoModels, atributoModels,
				enumeracionModels, mapeoModels);
	}

	@Transactional(readOnly = true)
	public String obtenerPlantilla(Long versionDocumentoXsdId) {
		return obtenerVersion(versionDocumentoXsdId).getPlantillaJson();
	}

	@Transactional(readOnly = true)
	public String obtenerEsquema(Long versionDocumentoXsdId) {
		return obtenerVersion(versionDocumentoXsdId).getEsquemaJson();
	}

	private VersionDocumentoXsd obtenerVersion(Long id) {
		return versionRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
				.orElseThrow(() -> new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA, id));
	}

	private VersionDocumentoXsdModel crearVersionModel(VersionDocumentoXsd version) {
		return new VersionDocumentoXsdModel(version.getId(), version.getDocumentoXsd().getId(), version.getVersion(),
				version.getNombreArchivo(), version.getRutaXsd(), version.getHashXsd(), version.getNamespaceXml(),
				version.getElementoRaiz(), version.getPlantillaJson(), version.getEsquemaJson(),
				version.getFechaInicio(), version.getFechaFin());
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
