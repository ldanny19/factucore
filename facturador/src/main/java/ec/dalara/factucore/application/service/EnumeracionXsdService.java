package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.EnumeracionXsdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnumeracionXsdService extends BaseService<EnumeracionXsd> {

	private final EnumeracionXsdRepository enumeracionXsdRepository;

	@Override
	protected BaseRepository<EnumeracionXsd, Long> getRepository() {
		return enumeracionXsdRepository;
	}

	@Override
	@Transactional
	public EnumeracionXsd guardar(EnumeracionXsd e){
		if(e==null) throw new ApplicationException(MessageCodes.ENUMERACION_XSD_REQUERIDA);
		if(e.getElementoXsd()==null||e.getElementoXsd().getId()==null) throw new ApplicationException(MessageCodes.ENUMERACION_XSD_ELEMENTO_REQUERIDO);
		if(e.getValor()==null||e.getValor().isBlank()) throw new ApplicationException(MessageCodes.ENUMERACION_XSD_VALOR_REQUERIDO);
		boolean dup=e.getId()==null?enumeracionXsdRepository.existsByElementoXsdIdAndValor(e.getElementoXsd().getId(),e.getValor()):enumeracionXsdRepository.existsByElementoXsdIdAndValorAndIdNot(e.getElementoXsd().getId(),e.getValor(),e.getId());
		if(dup) throw new ApplicationException(MessageCodes.ENUMERACION_XSD_DUPLICADA,e.getValor());
		return super.guardar(e);
	}

	public List<EnumeracionXsd> listarPorElementoXsd(Long elementoXsdId) {
		return enumeracionXsdRepository.findByElementoXsdId(elementoXsdId).stream()
				.filter(enumeracion -> !EstadoRegistro.ELIMINADO.equals(enumeracion.getEstadoRegistro())).toList();
	}

	public Optional<EnumeracionXsd> obtenerPorElementoXsdYValor(Long elementoXsdId, String valor) {
		return enumeracionXsdRepository.findByElementoXsdIdAndValor(elementoXsdId, valor)
				.filter(enumeracion -> EstadoRegistro.ACTIVO.equals(enumeracion.getEstadoRegistro()));
	}

	public boolean existePorElementoXsdYValor(Long elementoXsdId, String valor) {
		return enumeracionXsdRepository.findByElementoXsdIdAndValor(elementoXsdId, valor)
				.filter(enumeracion -> !EstadoRegistro.ELIMINADO.equals(enumeracion.getEstadoRegistro())).isPresent();
	}
}