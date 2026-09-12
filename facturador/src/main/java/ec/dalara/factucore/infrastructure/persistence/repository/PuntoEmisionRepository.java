package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuntoEmisionRepository extends BaseRepository<PuntoEmision, Long> {

    List<PuntoEmision> findByEstablecimientoId(Long establecimientoId);

    Optional<PuntoEmision> findByEstablecimientoIdAndCodigo(
            Long establecimientoId,
            String codigo
    );

    boolean existsByEstablecimientoIdAndCodigo(
            Long establecimientoId,
            String codigo
    );
}