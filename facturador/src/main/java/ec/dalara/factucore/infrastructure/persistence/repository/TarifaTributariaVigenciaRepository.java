package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.infrastructure.persistence.entity.TarifaTributariaVigencia;

public interface TarifaTributariaVigenciaRepository
        extends BaseRepository<TarifaTributariaVigencia, Long> {

    List<TarifaTributariaVigencia> findByCodigoImpuestoAndCodigoPorcentajeOrderByFechaVigenciaDesdeAsc(
            String codigoImpuesto, String codigoPorcentaje);

    @Query("""
            SELECT t
            FROM TarifaTributariaVigencia t
            WHERE t.codigoImpuesto = :codigoImpuesto
              AND t.codigoPorcentaje = :codigoPorcentaje
              AND t.fechaVigenciaDesde <= :fecha
              AND (t.fechaVigenciaHasta IS NULL OR t.fechaVigenciaHasta >= :fecha)
            """)
    Optional<TarifaTributariaVigencia> buscarVigente(
            @Param("codigoImpuesto") String codigoImpuesto,
            @Param("codigoPorcentaje") String codigoPorcentaje,
            @Param("fecha") LocalDate fecha);

    @Query("""
            SELECT t
            FROM TarifaTributariaVigencia t
            WHERE t.codigoImpuesto = :codigoImpuesto
              AND t.fechaVigenciaDesde <= :fecha
              AND (t.fechaVigenciaHasta IS NULL OR t.fechaVigenciaHasta >= :fecha)
            ORDER BY t.tarifa DESC
            """)
    List<TarifaTributariaVigencia> buscarVigentesPorImpuesto(
            @Param("codigoImpuesto") String codigoImpuesto,
            @Param("fecha") LocalDate fecha);

    @Query("""
            SELECT COUNT(t) > 0
            FROM TarifaTributariaVigencia t
            WHERE t.codigoImpuesto = :codigoImpuesto
              AND t.codigoPorcentaje = :codigoPorcentaje
              AND t.id <> :id
              AND t.estadoRegistro <> 'E'
              AND :desde <= COALESCE(t.fechaVigenciaHasta, :fechaMaxima)
              AND t.fechaVigenciaDesde <= COALESCE(:hasta, :fechaMaxima)
            """)
    boolean existeSolapamiento(
            @Param("codigoImpuesto") String codigoImpuesto,
            @Param("codigoPorcentaje") String codigoPorcentaje,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("id") Long id,
            @Param("fechaMaxima") LocalDate fechaMaxima);
}
