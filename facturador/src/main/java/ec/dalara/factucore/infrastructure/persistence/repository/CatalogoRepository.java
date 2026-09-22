package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;

public interface CatalogoRepository extends BaseRepository<Catalogo, Long> {

	Optional<Catalogo> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);
}