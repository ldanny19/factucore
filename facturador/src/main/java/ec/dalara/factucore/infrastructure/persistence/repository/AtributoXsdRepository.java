package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.AtributoXsd;

import java.util.List;
import java.util.Optional;

public interface AtributoXsdRepository
        extends BaseRepository<AtributoXsd, Long> {

    List<AtributoXsd> findByElementoXsdId(Long elementoXsdId);

    Optional<AtributoXsd> findByElementoXsdIdAndNombre(
            Long elementoXsdId,
            String nombre
    );
}