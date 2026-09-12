package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobanteInformacionAdicionalRepository
        extends BaseRepository<ComprobanteInformacionAdicional, Long> {

    List<ComprobanteInformacionAdicional> findByComprobanteId(
            Long comprobanteId
    );
}