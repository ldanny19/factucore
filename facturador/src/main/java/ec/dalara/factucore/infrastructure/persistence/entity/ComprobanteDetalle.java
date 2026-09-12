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
@Table(name = "comprobante_detalle")
public class ComprobanteDetalle implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante_detalle")
    @TableGenerator(
        name = "generador_comprobante_detalle",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "comprobante_detalle",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "numero_linea", nullable = false)
    private Integer numeroLinea;

    @Column(name = "codigo_principal", length = 100)
    private String codigoPrincipal;

    @Column(name = "codigo_auxiliar", length = 100)
    private String codigoAuxiliar;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad", precision = 18, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 18, scale = 6)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", precision = 18, scale = 6)
    private BigDecimal descuento;

    @Column(name = "precio_total_sin_impuesto", precision = 18, scale = 6)
    private BigDecimal precioTotalSinImpuesto;

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