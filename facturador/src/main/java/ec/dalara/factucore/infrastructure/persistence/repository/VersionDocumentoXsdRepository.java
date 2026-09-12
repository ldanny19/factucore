package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.VersionDocumentoXsd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VersionDocumentoXsdRepository
        extends BaseRepository<VersionDocumentoXsd, Long> {

    List<VersionDocumentoXsd> findByDocumentoXsdId(Long documentoXsdId);

    Optional<VersionDocumentoXsd> findByDocumentoXsdIdAndVersion(
            Long documentoXsdId,
            String version
    );

    boolean existsByDocumentoXsdIdAndVersion(
            Long documentoXsdId,
            String version
    );
}