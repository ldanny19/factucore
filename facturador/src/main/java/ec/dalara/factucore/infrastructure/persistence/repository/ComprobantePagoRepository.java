package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;

public interface ComprobantePagoRepository extends BaseRepository<ComprobantePago, Long> {

	List<ComprobantePago> findByComprobanteId(Long comprobanteId);
}