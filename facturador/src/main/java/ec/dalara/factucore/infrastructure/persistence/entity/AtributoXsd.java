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
@Table(name = "atributo_xsd")
public class AtributoXsd implements EstadoRegistroEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_atributo_xsd")
    @TableGenerator(
        name = "generador_atributo_xsd",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "atributo_xsd",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elemento_xsd_id", nullable = false)
    private ElementoXsd elementoXsd;

    @Column(name = "nombre", nullable = false, length = 300)
    private String nombre;

    @Column(name = "tipo_dato", length = 100)
    private String tipoDato;

    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio;

    @Column(name = "valor_predeterminado", length = 1000)
    private String valorPredeterminado;

    @Column(name = "patron", length = 2000)
    private String patron;

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