package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteDetalleImpuestoRepository
        extends BaseRepository<ComprobanteDetalleImpuesto, Long> {

    List<ComprobanteDetalleImpuesto> findByComprobanteDetalleId(
            Long comprobanteDetalleId
    );
}