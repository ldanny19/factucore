package ec.dalara.factucore.application.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ec.dalara.factucore.application.ApplicationException;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteAuditoriaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteAuditoriaService extends BaseService<ComprobanteAuditoria> {

	private final ComprobanteAuditoriaRepository comprobanteAuditoriaRepository;

	@Override
	protected BaseRepository<ComprobanteAuditoria, Long> getRepository() {
		return comprobanteAuditoriaRepository;
	}

	@Override
	@Transactional
	public ComprobanteAuditoria guardar(ComprobanteAuditoria entidad) {
		throw new ApplicationException("FACTUCORE.COMPROBANTE_AUDITORIA.SOLO_LECTURA");
	}

	@Override
	@Transactional
	public ComprobanteAuditoria inactivar(Long id) {
		throw new ApplicationException("FACTUCORE.COMPROBANTE_AUDITORIA.SOLO_LECTURA");
	}

	@Override
	@Transactional
	public ComprobanteAuditoria reactivar(Long id) {
		throw new ApplicationException("FACTUCORE.COMPROBANTE_AUDITORIA.SOLO_LECTURA");
	}

	@Override
	@Transactional
	public ComprobanteAuditoria eliminar(Long id) {
		throw new ApplicationException("FACTUCORE.COMPROBANTE_AUDITORIA.SOLO_LECTURA");
	}

	public List<ComprobanteAuditoria> listarPorComprobante(Long comprobanteId) {
		return comprobanteAuditoriaRepository.findByComprobanteIdOrderByFechaCreacionDesc(comprobanteId).stream()
				.filter(auditoria -> !EstadoRegistro.ELIMINADO.equals(auditoria.getEstadoRegistro())).toList();
	}
}