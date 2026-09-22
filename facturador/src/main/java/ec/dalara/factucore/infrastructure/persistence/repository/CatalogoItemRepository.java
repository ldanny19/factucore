package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;

public interface CatalogoItemRepository extends BaseRepository<CatalogoItem, Long> {

	List<CatalogoItem> findByCatalogoId(Long catalogoId);

	Optional<CatalogoItem> findByCatalogoIdAndCodigo(Long catalogoId, String codigo);

	boolean existsByCatalogoIdAndCodigo(Long catalogoId, String codigo);
}