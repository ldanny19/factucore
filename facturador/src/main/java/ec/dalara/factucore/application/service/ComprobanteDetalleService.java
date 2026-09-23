package ec.dalara.factucore.application.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

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

	@Override
	@Transactional
	public ComprobanteDetalle guardar(ComprobanteDetalle e) {
		if (e == null) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_REQUERIDO);
		if (e.getComprobante() == null || e.getComprobante().getId() == null) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_COMPROBANTE_REQUERIDO);
		if (e.getNumeroLinea() == null || e.getNumeroLinea() <= 0) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_LINEA_INVALIDA);
		boolean dup=e.getId()==null?comprobanteDetalleRepository.existsByComprobanteIdAndNumeroLinea(e.getComprobante().getId(),e.getNumeroLinea()):comprobanteDetalleRepository.existsByComprobanteIdAndNumeroLineaAndIdNot(e.getComprobante().getId(),e.getNumeroLinea(),e.getId());
		if(dup) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_LINEA_DUPLICADA,e.getNumeroLinea());
		return super.guardar(e);
	}

	public List<ComprobanteDetalle> listarPorComprobante(Long comprobanteId) {
		return comprobanteDetalleRepository.findByComprobanteIdOrderByNumeroLineaAsc(comprobanteId).stream()
				.filter(detalle -> !EstadoRegistro.ELIMINADO.equals(detalle.getEstadoRegistro())).toList();
	}
}