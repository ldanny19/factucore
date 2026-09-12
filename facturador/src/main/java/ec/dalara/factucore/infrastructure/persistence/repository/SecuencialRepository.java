package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecuencialRepository extends BaseRepository<Secuencial, Long> {

    Optional<Secuencial> findByPuntoEmisionIdAndCodigoDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    );

    boolean existsByPuntoEmisionIdAndCodigoDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    );
}