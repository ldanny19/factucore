package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ElementoXsd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElementoXsdRepository extends BaseRepository<ElementoXsd, Long> {

    List<ElementoXsd> findByVersionDocumentoXsdId(
            Long versionDocumentoXsdId
    );

    List<ElementoXsd> findByElementoPadreId(
            Long elementoPadreId
    );
}