package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.Optional;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public interface ComprobanteRepository extends BaseRepository<Comprobante, Long> {

	Optional<Comprobante> findByClaveAcceso(String claveAcceso);

	boolean existsByClaveAcceso(String claveAcceso);

	Optional<Comprobante> findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(
			Long empresaId, Long establecimientoId, Long puntoEmisionId, String codigoDocumento, String secuencial);
}