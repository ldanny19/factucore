package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;

public interface ComprobanteDetalleImpuestoRepository extends BaseRepository<ComprobanteDetalleImpuesto, Long> {

	List<ComprobanteDetalleImpuesto> findByComprobanteDetalleId(Long comprobanteDetalleId);
}