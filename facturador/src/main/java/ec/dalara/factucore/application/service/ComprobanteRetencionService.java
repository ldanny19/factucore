package ec.dalara.factucore.application.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

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

	@Override
	@Transactional
	public ComprobanteRetencion guardar(ComprobanteRetencion e){
		if(e==null) throw new ApplicationException(MessageCodes.COMPROBANTE_RETENCION_REQUERIDA);
		if(e.getComprobante()==null||e.getComprobante().getId()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_RETENCION_COMPROBANTE_REQUERIDO);
		if(e.getCodigoImpuesto()==null||e.getCodigoImpuesto().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_RETENCION_CODIGO_IMPUESTO_REQUERIDO);
		if(e.getCodigoRetencion()==null||e.getCodigoRetencion().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_RETENCION_CODIGO_RETENCION_REQUERIDO);
		if(e.getBaseImponible()==null||e.getValorRetenido()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_RETENCION_VALORES_REQUERIDOS);
		return super.guardar(e);
	}
	
	public List<ComprobanteRetencion> listarPorComprobante(Long comprobanteId) {
		return comprobanteRetencionRepository.findByComprobanteId(comprobanteId).stream()
				.filter(retencion -> !EstadoRegistro.ELIMINADO.equals(retencion.getEstadoRegistro())).toList();
	}
}