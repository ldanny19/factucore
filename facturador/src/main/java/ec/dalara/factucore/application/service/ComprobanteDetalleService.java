package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteDetalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteDetalleService extends BaseService<ComprobanteDetalle> {

    private final ComprobanteDetalleRepository comprobanteDetalleRepository;

    @Override
    protected BaseRepository<ComprobanteDetalle, Long> getRepository() {
        return comprobanteDetalleRepository;
    }

    public List<ComprobanteDetalle> listarPorComprobante(
            Long comprobanteId
    ) {
        return comprobanteDetalleRepository
                .findByComprobanteIdOrderByNumeroLineaAsc(comprobanteId)
                .stream()
                .filter(detalle ->
                        !EstadoRegistro.ELIMINADO.equals(
                                detalle.getEstadoRegistro()
                        )
                )
                .toList();
    }
}