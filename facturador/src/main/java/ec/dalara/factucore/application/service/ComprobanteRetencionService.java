package ec.dalara.factucore.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRetencionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteRetencionService extends BaseService<ComprobanteRetencion> {

	private final ComprobanteRetencionRepository comprobanteRetencionRepository;

	@Override
	protected BaseRepository<ComprobanteRetencion, Long> getRepository() {
		return comprobanteRetencionRepository;
	}

	public List<ComprobanteRetencion> listarPorComprobante(Long comprobanteId) {
		return comprobanteRetencionRepository.findByComprobanteId(comprobanteId).stream()
				.filter(retencion -> !EstadoRegistro.ELIMINADO.equals(retencion.getEstadoRegistro())).toList();
	}
}