package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentoXsdRepository extends BaseRepository<DocumentoXsd, Long> {

    Optional<DocumentoXsd> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}