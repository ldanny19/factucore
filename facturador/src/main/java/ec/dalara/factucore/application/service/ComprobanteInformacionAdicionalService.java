package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteInformacionAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteInformacionAdicionalService
        extends BaseService<ComprobanteInformacionAdicional> {

    private final ComprobanteInformacionAdicionalRepository comprobanteInformacionAdicionalRepository;

    @Override
    protected BaseRepository<ComprobanteInformacionAdicional, Long> getRepository() {
        return comprobanteInformacionAdicionalRepository;
    }

    public List<ComprobanteInformacionAdicional> listarPorComprobante(
            Long comprobanteId
    ) {
        return comprobanteInformacionAdicionalRepository
                .findByComprobanteId(comprobanteId)
                .stream()
                .filter(informacion ->
                        !EstadoRegistro.ELIMINADO.equals(
                                informacion.getEstadoRegistro()
                        )
                )
                .toList();
    }
}