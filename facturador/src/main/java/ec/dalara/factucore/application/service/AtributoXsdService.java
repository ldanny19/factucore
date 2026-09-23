package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.AtributoXsdRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtributoXsdService extends BaseService<AtributoXsd> {

	private final AtributoXsdRepository atributoXsdRepository;

	@Override
	protected BaseRepository<AtributoXsd, Long> getRepository() {
		return atributoXsdRepository;
	}

	@Override
	@Transactional
	public AtributoXsd guardar(AtributoXsd e) {
		validar(e);
		boolean dup=e.getId()==null?atributoXsdRepository.existsByElementoXsdIdAndNombre(e.getElementoXsd().getId(),e.getNombre()):atributoXsdRepository.existsByElementoXsdIdAndNombreAndIdNot(e.getElementoXsd().getId(),e.getNombre(),e.getId());
		if(dup) throw new ApplicationException(MessageCodes.ATRIBUTO_XSD_DUPLICADO,e.getNombre());
		return super.guardar(e);
	}
	private void validar(AtributoXsd e){
		if(e==null) throw new ApplicationException(MessageCodes.ATRIBUTO_XSD_REQUERIDO);
		if(e.getElementoXsd()==null||e.getElementoXsd().getId()==null) throw new ApplicationException(MessageCodes.ATRIBUTO_XSD_ELEMENTO_REQUERIDO);
		if(e.getNombre()==null||e.getNombre().isBlank()) throw new ApplicationException(MessageCodes.ATRIBUTO_XSD_NOMBRE_REQUERIDO);
		if(e.getObligatorio()==null) throw new ApplicationException(MessageCodes.ATRIBUTO_XSD_OBLIGATORIO_REQUERIDO);
	}

	public List<AtributoXsd> listarPorElementoXsd(Long elementoXsdId) {
		return atributoXsdRepository.findByElementoXsdId(elementoXsdId).stream()
				.filter(atributo -> !EstadoRegistro.ELIMINADO.equals(atributo.getEstadoRegistro())).toList();
	}

	public Optional<AtributoXsd> obtenerPorElementoXsdYNombre(Long elementoXsdId, String nombre) {
		return atributoXsdRepository.findByElementoXsdIdAndNombre(elementoXsdId, nombre)
				.filter(atributo -> EstadoRegistro.ACTIVO.equals(atributo.getEstadoRegistro()));
	}
}