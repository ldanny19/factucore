package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public interface ComprobanteRepository extends BaseRepository<Comprobante, Long> {

	Optional<Comprobante> findByClaveAcceso(String claveAcceso);

	boolean existsByClaveAcceso(String claveAcceso);

	Optional<Comprobante> findByEmpresaIdAndEstablecimientoIdAndPuntoEmisionIdAndCodigoDocumentoAndSecuencial(
			Long empresaId, Long establecimientoId, Long puntoEmisionId, String codigoDocumento, String secuencial);

    List<Comprobante> findTop100ByEstadoProcesoAndFechaProximoReprocesoLessThanEqualOrderByFechaProximoReprocesoAsc(
            String estadoProceso, LocalDateTime fecha);

    @Modifying
    @Query("""
            update Comprobante c
               set c.fechaProximoReproceso = :bloqueadoHasta
             where c.id = :id
               and c.estadoProceso = :estadoProceso
               and c.fechaProximoReproceso <= :ahora
            """)
    int reclamarReproceso(@Param("id") Long id,
            @Param("estadoProceso") String estadoProceso,
            @Param("ahora") LocalDateTime ahora,
            @Param("bloqueadoHasta") LocalDateTime bloqueadoHasta);
}