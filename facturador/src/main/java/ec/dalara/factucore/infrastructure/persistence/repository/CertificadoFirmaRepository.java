package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificadoFirmaRepository extends BaseRepository<CertificadoFirma, Long> {

    List<CertificadoFirma> findByEmpresaId(Long empresaId);
}