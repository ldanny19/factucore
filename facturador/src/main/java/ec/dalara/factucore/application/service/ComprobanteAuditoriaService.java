package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteAuditoria;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteAuditoriaService
        extends BaseService<ComprobanteAuditoria> {

    private final ComprobanteAuditoriaRepository comprobanteAuditoriaRepository;

    @Override
    protected BaseRepository<ComprobanteAuditoria, Long> getRepository() {
        return comprobanteAuditoriaRepository;
    }

    public List<ComprobanteAuditoria> listarPorComprobante(
            Long comprobanteId
    ) {
        return comprobanteAuditoriaRepository
                .findByComprobanteIdOrderByFechaCreacionDesc(comprobanteId)
                .stream()
                .filter(auditoria ->
                        !EstadoRegistro.ELIMINADO.equals(
                                auditoria.getEstadoRegistro()
                        )
                )
                .toList();
    }
}