package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CertificadoFirmaRepository
        extends BaseRepository<CertificadoFirma, Long> {

    List<CertificadoFirma> findByEmpresaId(Long empresaId);

    Optional<CertificadoFirma> findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long empresaId,
            String estadoRegistro,
            LocalDateTime fecha
    );

    Optional<CertificadoFirma> findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
            Long empresaId,
            String estadoRegistro,
            LocalDateTime fecha
    );
}