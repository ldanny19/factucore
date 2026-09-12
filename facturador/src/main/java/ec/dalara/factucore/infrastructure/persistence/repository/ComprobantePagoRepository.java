package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobantePagoRepository
        extends BaseRepository<ComprobantePago, Long> {

    List<ComprobantePago> findByComprobanteId(
            Long comprobanteId
    );
}