package ec.dalara.factucore.infrastructure.persistence.repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComprobanteRepository extends BaseRepository<Comprobante, Long> {

    Optional<Comprobante> findByClaveAcceso(String claveAcceso);

    boolean existsByClaveAcceso(String claveAcceso);

    Optional<Comprobante> findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(
            Long empresaId,
            Long establecimientoId,
            Long puntoEmisionId,
            String codigoDocumento,
            String secuencial
    );
}