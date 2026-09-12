package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobante_detalle_impuesto")
public class ComprobanteDetalleImpuesto implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante_detalle_impuesto")
    @TableGenerator(
        name = "generador_comprobante_detalle_impuesto",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "comprobante_detalle_impuesto",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_detalle_id", nullable = false)
    private ComprobanteDetalle comprobanteDetalle;

    @Column(name = "codigo_impuesto", nullable = false, length = 20)
    private String codigoImpuesto;

    @Column(name = "codigo_porcentaje", nullable = false, length = 20)
    private String codigoPorcentaje;

    @Column(name = "tarifa", precision = 10, scale = 4)
    private BigDecimal tarifa;

    @Column(name = "base_imponible", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseImponible;

    @Column(name = "valor", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_devolucion_iva", precision = 14, scale = 2)
    private BigDecimal valorDevolucionIva;

    @Column(name = "estado_registro", nullable = false, length = 20)
    private String estadoRegistro;

    @Column(name = "usuario_creacion", nullable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "fecha_creacion", nullable = false)
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private java.time.LocalDateTime fechaModificacion;

    @Column(name = "observacion", length = 500)
    private String observacion;
}