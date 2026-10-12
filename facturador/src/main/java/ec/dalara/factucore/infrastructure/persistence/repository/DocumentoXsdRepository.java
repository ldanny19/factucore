package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.DocumentoXsd;
import jakarta.persistence.LockModeType;

public interface DocumentoXsdRepository extends BaseRepository<DocumentoXsd, Long> {

	Optional<DocumentoXsd> findByCodigo(String codigo);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from DocumentoXsd d where d.codigo = :codigo")
	Optional<DocumentoXsd> findByCodigoForUpdate(@Param("codigo") String codigo);

	boolean existsByCodigo(String codigo);

	boolean existsByCodigoAndIdNot(String codigo, Long id);
}