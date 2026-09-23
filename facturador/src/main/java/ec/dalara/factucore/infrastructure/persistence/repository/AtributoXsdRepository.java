package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;

public interface AtributoXsdRepository extends BaseRepository<AtributoXsd, Long> {

	List<AtributoXsd> findByElementoXsdId(Long elementoXsdId);

	Optional<AtributoXsd> findByElementoXsdIdAndNombre(Long elementoXsdId, String nombre);

	boolean existsByElementoXsdIdAndNombre(Long elementoXsdId, String nombre);
	boolean existsByElementoXsdIdAndNombreAndIdNot(Long elementoXsdId, String nombre, Long id);
}