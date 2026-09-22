package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;

public interface ComprobanteDetalleRepository extends BaseRepository<ComprobanteDetalle, Long> {

	List<ComprobanteDetalle> findByComprobanteIdOrderByNumeroLineaAsc(Long comprobanteId);
}