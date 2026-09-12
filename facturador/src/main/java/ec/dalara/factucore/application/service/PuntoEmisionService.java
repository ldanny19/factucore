package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.PuntoEmisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PuntoEmisionService extends BaseService<PuntoEmision> {

    private final PuntoEmisionRepository puntoEmisionRepository;

    @Override
    protected BaseRepository<PuntoEmision, Long> getRepository() {
        return puntoEmisionRepository;
    }

    public List<PuntoEmision> listarPorEstablecimiento(
            Long establecimientoId
    ) {
        return puntoEmisionRepository
                .findByEstablecimientoId(establecimientoId)
                .stream()
                .filter(puntoEmision ->
                        !EstadoRegistro.ELIMINADO.equals(
                                puntoEmision.getEstadoRegistro()
                        )
                )
                .toList();
    }

    public Optional<PuntoEmision> obtenerPorEstablecimientoYCodigo(
            Long establecimientoId,
            String codigo
    ) {
        return puntoEmisionRepository
                .findByEstablecimientoIdAndCodigo(
                        establecimientoId,
                        codigo
                )
                .filter(puntoEmision ->
                        EstadoRegistro.ACTIVO.equals(
                                puntoEmision.getEstadoRegistro()
                        )
                );
    }

    public boolean existePorEstablecimientoYCodigo(
            Long establecimientoId,
            String codigo
    ) {
        return puntoEmisionRepository
                .findByEstablecimientoIdAndCodigo(
                        establecimientoId,
                        codigo
                )
                .filter(puntoEmision ->
                        !EstadoRegistro.ELIMINADO.equals(
                                puntoEmision.getEstadoRegistro()
                        )
                )
                .isPresent();
    }
}