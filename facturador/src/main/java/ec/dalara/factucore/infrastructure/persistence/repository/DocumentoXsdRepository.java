package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;

public interface DocumentoXsdRepository extends BaseRepository<DocumentoXsd, Long> {

	Optional<DocumentoXsd> findByCodigo(String codigo);

	boolean existsByCodigo(String codigo);
}