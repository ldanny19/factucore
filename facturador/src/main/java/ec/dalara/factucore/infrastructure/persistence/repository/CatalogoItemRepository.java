package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogoItemRepository extends BaseRepository<CatalogoItem, Long> {

    List<CatalogoItem> findByCatalogoId(Long catalogoId);

    Optional<CatalogoItem> findByCatalogoIdAndCodigo(
            Long catalogoId,
            String codigo
    );

    boolean existsByCatalogoIdAndCodigo(
            Long catalogoId,
            String codigo
    );
}