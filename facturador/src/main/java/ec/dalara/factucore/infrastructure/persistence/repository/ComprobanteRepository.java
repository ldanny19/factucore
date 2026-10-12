package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public interface ComprobanteRepository extends BaseRepository<Comprobante, Long> {

	@EntityGraph(attributePaths = "empresa")
	@Query("select c from Comprobante c where c.id = :id")
	Optional<Comprobante> buscarParaRide(@Param("id") Long id);

	Optional<Comprobante> findByClaveAcceso(String claveAcceso);

	List<Comprobante> findByEstadoProcesoAndEstadoRegistro(String estadoProceso, String estadoRegistro);

	Optional<Comprobante> findByEmpresaIdAndIdTransaccion(Long empresaId, String idTransaccion);

	Optional<Comprobante> findFirstByEmpresaIdAndIdDocumentoOrigenAndEstadoRegistroOrderByFechaCreacionDesc(
			Long empresaId, Long idDocumentoOrigen, String estadoRegistro);

	boolean existsByEmpresaIdAndIdTransaccion(Long empresaId, String idTransaccion);

	Optional<Comprobante> findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(
			Long empresaId, Long establecimientoId, Long puntoEmisionId, String codigoDocumento, String secuencial);
}