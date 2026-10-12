package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;

public interface ComprobanteInformacionAdicionalRepository
		extends BaseRepository<ComprobanteInformacionAdicional, Long> {

	List<ComprobanteInformacionAdicional> findByComprobanteId(Long comprobanteId);
}