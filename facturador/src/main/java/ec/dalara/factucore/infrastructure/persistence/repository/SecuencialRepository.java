package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface SecuencialRepository
        extends BaseRepository<Secuencial, Long> {

    Optional<Secuencial> findByPuntoEmisionIdAndCodigoDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Secuencial> findByPuntoEmisionIdAndCodigoDocumentoAndEstadoRegistro(
            Long puntoEmisionId,
            String codigoDocumento,
            String estadoRegistro
    );

    boolean existsByPuntoEmisionIdAndCodigoDocumento(
            Long puntoEmisionId,
            String codigoDocumento
    );
}