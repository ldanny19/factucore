package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;

public interface ComprobanteRetencionRepository extends BaseRepository<ComprobanteRetencion, Long> {

	List<ComprobanteRetencion> findByComprobanteId(Long comprobanteId);
}