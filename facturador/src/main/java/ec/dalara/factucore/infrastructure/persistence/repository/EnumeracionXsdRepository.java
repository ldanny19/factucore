package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.EnumeracionXsd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnumeracionXsdRepository
        extends BaseRepository<EnumeracionXsd, Long> {

    List<EnumeracionXsd> findByElementoXsdId(Long elementoXsdId);

    Optional<EnumeracionXsd> findByElementoXsdIdAndValor(
            Long elementoXsdId,
            String valor
    );

    boolean existsByElementoXsdIdAndValor(
            Long elementoXsdId,
            String valor
    );
    
    
}