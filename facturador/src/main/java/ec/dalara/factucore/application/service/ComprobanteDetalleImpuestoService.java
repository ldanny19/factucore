package ec.dalara.factucore.application.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteDetalleImpuestoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteDetalleImpuestoService extends BaseService<ComprobanteDetalleImpuesto> {

	private final ComprobanteDetalleImpuestoRepository comprobanteDetalleImpuestoRepository;

	@Override
	protected BaseRepository<ComprobanteDetalleImpuesto, Long> getRepository() {
		return comprobanteDetalleImpuestoRepository;
	}

	@Override
	@Transactional
	public ComprobanteDetalleImpuesto guardar(ComprobanteDetalleImpuesto e){
		if(e==null) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_IMPUESTO_REQUERIDO);
		if(e.getComprobanteDetalle()==null||e.getComprobanteDetalle().getId()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_IMPUESTO_DETALLE_REQUERIDO);
		if(e.getCodigoImpuesto()==null||e.getCodigoImpuesto().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_IMPUESTO_CODIGO_IMPUESTO_REQUERIDO);
		if(e.getCodigoPorcentaje()==null||e.getCodigoPorcentaje().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_IMPUESTO_CODIGO_PORCENTAJE_REQUERIDO);
		if(e.getBaseImponible()==null||e.getValor()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_DETALLE_IMPUESTO_VALORES_REQUERIDOS);
		return super.guardar(e);
	}
	
	public List<ComprobanteDetalleImpuesto> listarPorComprobanteDetalle(Long comprobanteDetalleId) {
		return comprobanteDetalleImpuestoRepository.findByComprobanteDetalleId(comprobanteDetalleId).stream()
				.filter(impuesto -> !EstadoRegistro.ELIMINADO.equals(impuesto.getEstadoRegistro())).toList();
	}
}