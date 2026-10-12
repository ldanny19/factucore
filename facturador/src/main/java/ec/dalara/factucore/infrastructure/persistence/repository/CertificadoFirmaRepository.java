package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;

public interface CertificadoFirmaRepository extends BaseRepository<CertificadoFirma, Long> {

	List<CertificadoFirma> findByEmpresaId(Long empresaId);

	@Query("SELECT c FROM CertificadoFirma c WHERE c.empresa.id = :empresaId AND c.estadoRegistro = :estadoRegistro AND c.fechaInicio <= :fecha AND c.fechaFin >= :fecha")
	Optional<CertificadoFirma> findCertificadoVigenteConFechaFin(@Param("empresaId") Long empresaId,
			@Param("estadoRegistro") String estadoRegistro, @Param("fecha") LocalDateTime fecha);

	Optional<CertificadoFirma> findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
			Long empresaId, String estadoRegistro, LocalDateTime fecha);
}