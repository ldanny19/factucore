package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteDetalleRepository
        extends BaseRepository<ComprobanteDetalle, Long> {

    List<ComprobanteDetalle> findByComprobanteIdOrderByNumeroLineaAsc(
            Long comprobanteId
    );
}