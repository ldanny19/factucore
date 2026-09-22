package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;

public interface CertificadoFirmaRepository extends BaseRepository<CertificadoFirma, Long> {

	List<CertificadoFirma> findByEmpresaId(Long empresaId);

	Optional<CertificadoFirma> findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
			Long empresaId, String estadoRegistro, LocalDateTime fecha);

	Optional<CertificadoFirma> findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
			Long empresaId, String estadoRegistro, LocalDateTime fecha);
}