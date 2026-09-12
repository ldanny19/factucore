package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobante_retencion")
public class ComprobanteRetencion implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante_retencion")
    @TableGenerator(
        name = "generador_comprobante_retencion",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "comprobante_retencion",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "codigo_impuesto", length = 20)
    private String codigoImpuesto;

    @Column(name = "codigo_retencion", length = 20)
    private String codigoRetencion;

    @Column(name = "porcentaje_retener", precision = 18, scale = 6)
    private BigDecimal porcentajeRetener;

    @Column(name = "base_imponible", precision = 18, scale = 6)
    private BigDecimal baseImponible;

    @Column(name = "valor_retenido", precision = 18, scale = 6)
    private BigDecimal valorRetenido;

    @Column(name = "numero_documento_sustento", length = 50)
    private String numeroDocumentoSustento;

    @Column(name = "fecha_emision_documento_sustento")
    private LocalDate fechaEmisionDocumentoSustento;

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