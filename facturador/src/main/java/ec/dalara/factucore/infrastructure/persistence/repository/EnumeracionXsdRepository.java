package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;

public interface EnumeracionXsdRepository extends BaseRepository<EnumeracionXsd, Long> {

	List<EnumeracionXsd> findByElementoXsdId(Long elementoXsdId);

	Optional<EnumeracionXsd> findByElementoXsdIdAndValor(Long elementoXsdId, String valor);

	boolean existsByElementoXsdIdAndValor(Long elementoXsdId, String valor);
	boolean existsByElementoXsdIdAndValorAndIdNot(Long elementoXsdId, String valor, Long id);
}