package ec.dalara.factucore.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteDetalleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteDetalleService extends BaseService<ComprobanteDetalle> {

	private final ComprobanteDetalleRepository comprobanteDetalleRepository;

	@Override
	protected BaseRepository<ComprobanteDetalle, Long> getRepository() {
		return comprobanteDetalleRepository;
	}

	public List<ComprobanteDetalle> listarPorComprobante(Long comprobanteId) {
		return comprobanteDetalleRepository.findByComprobanteIdOrderByNumeroLineaAsc(comprobanteId).stream()
				.filter(detalle -> !EstadoRegistro.ELIMINADO.equals(detalle.getEstadoRegistro())).toList();
	}
}