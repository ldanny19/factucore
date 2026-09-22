package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;

public interface ComprobanteAuditoriaRepository extends BaseRepository<ComprobanteAuditoria, Long> {

	List<ComprobanteAuditoria> findByComprobanteIdOrderByFechaCreacionDesc(Long comprobanteId);
}