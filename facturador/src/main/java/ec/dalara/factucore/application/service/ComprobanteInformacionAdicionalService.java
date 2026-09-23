package ec.dalara.factucore.application.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.MessageCodes;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteInformacionAdicionalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteInformacionAdicionalService extends BaseService<ComprobanteInformacionAdicional> {

	private final ComprobanteInformacionAdicionalRepository comprobanteInformacionAdicionalRepository;

	@Override
	protected BaseRepository<ComprobanteInformacionAdicional, Long> getRepository() {
		return comprobanteInformacionAdicionalRepository;
	}

	@Override
	@Transactional
	public ComprobanteInformacionAdicional guardar(ComprobanteInformacionAdicional e){
		if(e==null) throw new ApplicationException(MessageCodes.COMPROBANTE_INFO_ADICIONAL_REQUERIDA);
		if(e.getComprobante()==null||e.getComprobante().getId()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_INFO_ADICIONAL_COMPROBANTE_REQUERIDO);
		if(e.getNombre()==null||e.getNombre().isBlank()) throw new ApplicationException(MessageCodes.COMPROBANTE_INFO_ADICIONAL_NOMBRE_REQUERIDO);
		if(e.getValor()==null) throw new ApplicationException(MessageCodes.COMPROBANTE_INFO_ADICIONAL_VALOR_REQUERIDO);
		return super.guardar(e);
	}
	
	public List<ComprobanteInformacionAdicional> listarPorComprobante(Long comprobanteId) {
		return comprobanteInformacionAdicionalRepository.findByComprobanteId(comprobanteId).stream()
				.filter(informacion -> !EstadoRegistro.ELIMINADO.equals(informacion.getEstadoRegistro())).toList();
	}
}