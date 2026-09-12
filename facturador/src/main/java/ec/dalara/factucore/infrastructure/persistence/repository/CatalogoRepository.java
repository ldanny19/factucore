package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogoRepository extends BaseRepository<Catalogo, Long> {

    Optional<Catalogo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}