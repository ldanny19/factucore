package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteDetalleImpuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteDetalleImpuestoService
        extends BaseService<ComprobanteDetalleImpuesto> {

    private final ComprobanteDetalleImpuestoRepository comprobanteDetalleImpuestoRepository;

    @Override
    protected BaseRepository<ComprobanteDetalleImpuesto, Long> getRepository() {
        return comprobanteDetalleImpuestoRepository;
    }

    public List<ComprobanteDetalleImpuesto> listarPorComprobanteDetalle(
            Long comprobanteDetalleId
    ) {
        return comprobanteDetalleImpuestoRepository
                .findByComprobanteDetalleId(comprobanteDetalleId)
                .stream()
                .filter(impuesto ->
                        !EstadoRegistro.ELIMINADO.equals(
                                impuesto.getEstadoRegistro()
                        )
                )
                .toList();
    }
}