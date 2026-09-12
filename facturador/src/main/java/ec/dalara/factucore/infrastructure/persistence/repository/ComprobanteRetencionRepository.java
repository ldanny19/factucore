package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteRetencionRepository
        extends BaseRepository<ComprobanteRetencion, Long> {

    List<ComprobanteRetencion> findByComprobanteId(
            Long comprobanteId
    );
}