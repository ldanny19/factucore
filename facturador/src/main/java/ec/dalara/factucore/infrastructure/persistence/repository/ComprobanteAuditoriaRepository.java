package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteAuditoriaRepository
        extends BaseRepository<ComprobanteAuditoria, Long> {

    List<ComprobanteAuditoria> findByComprobanteIdOrderByFechaCreacionDesc(
            Long comprobanteId
    );
}