package ec.dalara.factucore.application.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ElementoXsdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ElementoXsdService extends BaseService<ElementoXsd> {

	private final ElementoXsdRepository elementoXsdRepository;

	@Override
	protected BaseRepository<ElementoXsd, Long> getRepository() {
		return elementoXsdRepository;
	}

	@Override
	@Transactional
	public ElementoXsd guardar(ElementoXsd e) {
		validar(e);
		if (e.getElementoPadre() != null && e.getElementoPadre().getVersionDocumentoXsd() != null && !e.getVersionDocumentoXsd().getId().equals(e.getElementoPadre().getVersionDocumentoXsd().getId())) throw new ApplicationException(MessageCodes.ELEMENTO_XSD.PADRE_VERSION_INVALIDA);
		return super.guardar(e);
	}

	private void validar(ElementoXsd e) {
		if (e == null) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_REQUERIDO);
		if (e.getVersionDocumentoXsd() == null || e.getVersionDocumentoXsd().getId() == null) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_VERSION_REQUERIDA);
		if (e.getNombre() == null || e.getNombre().isBlank()) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_NOMBRE_REQUERIDO);
		if (e.getObligatorio() == null || e.getRepetible() == null) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_INDICADORES_REQUERIDOS);
		if (e.getMinOcurrencias() != null && e.getMaxOcurrencias() != null && e.getMinOcurrencias() > e.getMaxOcurrencias()) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_OCURRENCIAS_INVALIDAS);
		if (e.getLongitudMinima() != null && e.getLongitudMaxima() != null && e.getLongitudMinima() > e.getLongitudMaxima()) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_LONGITUD_INVALIDA);
		if (e.getMinOcurrencias() != null && e.getMinOcurrencias() < 0 || e.getMaxOcurrencias() != null && e.getMaxOcurrencias() < 0) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_OCURRENCIAS_INVALIDAS);
		if (e.getFechaInicio() != null && e.getFechaFin() != null && e.getFechaFin().isBefore(e.getFechaInicio())) throw new ApplicationException(MessageCodes.ELEMENTO_XSD_RANGO_FECHAS_INVALIDO);
	}

	public List<ElementoXsd> listarPorVersionDocumentoXsd(Long versionDocumentoXsdId) {
		return elementoXsdRepository.findByVersionDocumentoXsdId(versionDocumentoXsdId).stream()
				.filter(elemento -> !EstadoRegistro.ELIMINADO.equals(elemento.getEstadoRegistro())).toList();
	}

	public List<ElementoXsd> listarPorElementoPadre(Long elementoPadreId) {
		return elementoXsdRepository.findByElementoPadreId(elementoPadreId).stream()
				.filter(elemento -> !EstadoRegistro.ELIMINADO.equals(elemento.getEstadoRegistro())).toList();
	}
}