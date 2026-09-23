package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteEvidencia;

public interface ComprobanteEvidenciaRepository extends JpaRepository<ComprobanteEvidencia, Long> {

    Optional<ComprobanteEvidencia> findByComprobanteIdAndTipoEvidenciaAndActualTrue(
            Long comprobanteId, String tipoEvidencia);
}