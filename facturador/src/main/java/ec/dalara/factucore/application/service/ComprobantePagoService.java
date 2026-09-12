package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobantePagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobantePagoService extends BaseService<ComprobantePago> {

    private final ComprobantePagoRepository comprobantePagoRepository;

    @Override
    protected BaseRepository<ComprobantePago, Long> getRepository() {
        return comprobantePagoRepository;
    }

    public List<ComprobantePago> listarPorComprobante(
            Long comprobanteId
    ) {
        return comprobantePagoRepository
                .findByComprobanteId(comprobanteId)
                .stream()
                .filter(pago ->
                        !EstadoRegistro.ELIMINADO.equals(
                                pago.getEstadoRegistro()
                        )
                )
                .toList();
    }
}