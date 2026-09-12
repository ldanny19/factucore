package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobante_informacion_adicional")
public class ComprobanteInformacionAdicional implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante_informacion_adicional")
    @TableGenerator(
        name = "generador_comprobante_informacion_adicional",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "comprobante_informacion_adicional",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "valor", length = 4000)
    private String valor;

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