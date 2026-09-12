package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "elemento_xsd")
public class ElementoXsd implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_elemento_xsd")
    @TableGenerator(
        name = "generador_elemento_xsd",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "elemento_xsd",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_documento_xsd_id", nullable = false)
    private VersionDocumentoXsd versionDocumentoXsd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_padre_id")
    private ElementoXsd elementoPadre;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "tipo_dato", length = 100)
    private String tipoDato;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio;

    @Column(name = "repetible", nullable = false)
    private Boolean repetible;

    @Column(name = "min_ocurrencias")
    private Integer minOcurrencias;

    @Column(name = "max_ocurrencias")
    private Integer maxOcurrencias;

    @Column(name = "longitud_minima")
    private Integer longitudMinima;

    @Column(name = "longitud_maxima")
    private Integer longitudMaxima;

    @Column(name = "digitos_totales")
    private Integer digitosTotales;

    @Column(name = "decimales")
    private Integer decimales;

    @Column(name = "valor_minimo", precision = 30, scale = 10)
    private BigDecimal valorMinimo;

    @Column(name = "valor_maximo", precision = 30, scale = 10)
    private BigDecimal valorMaximo;

    @Column(name = "patron", length = 2000)
    private String patron;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

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