package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;

public interface PuntoEmisionRepository extends BaseRepository<PuntoEmision, Long> {

    List<PuntoEmision> findByEstablecimientoId(Long establecimientoId);

    Optional<PuntoEmision> findByEstablecimientoIdAndCodigo(Long establecimientoId, String codigo);

    boolean existsByEstablecimientoIdAndCodigo(Long establecimientoId, String codigo);

    boolean existsByEstablecimientoIdAndCodigoAndIdNot(Long establecimientoId, String codigo, Long id);
}