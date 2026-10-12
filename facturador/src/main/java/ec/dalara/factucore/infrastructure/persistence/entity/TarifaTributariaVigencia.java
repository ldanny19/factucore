package ec.dalara.factucore.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarifa_tributaria_vigencia")
public class TarifaTributariaVigencia implements EstadoRegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_tarifa_tributaria_vigencia")
    @TableGenerator(name = "generador_tarifa_tributaria_vigencia", table = "hibernate_sequences",
            pkColumnName = "sequence_name", valueColumnName = "next_val",
            pkColumnValue = "tarifa_tributaria_vigencia", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalogo_item_id", nullable = false)
    private CatalogoItem catalogoItem;

    @Column(name = "codigo_impuesto", nullable = false, length = 2)
    private String codigoImpuesto;

    @Column(name = "codigo_porcentaje", nullable = false, length = 4)
    private String codigoPorcentaje;

    @Column(name = "tarifa", nullable = false, precision = 8, scale = 4)
    private BigDecimal tarifa;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDate fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;

    @Column(name = "estado_registro", nullable = false, length = 20)
    private String estadoRegistro;

    @Column(name = "usuario_creacion", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "observacion", length = 500)
    private String observacion;
}
