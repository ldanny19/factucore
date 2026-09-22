package ec.dalara.factucore.infrastructure.persistence.entity;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mapeo_xsd")
public class MapeoXsd implements EstadoRegistroEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE,
            generator = "generador_mapeo_xsd"
    )
    @TableGenerator(
            name = "generador_mapeo_xsd",
            table = "hibernate_sequences",
            pkColumnName = "sequence_name",
            valueColumnName = "next_val",
            pkColumnValue = "mapeo_xsd",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "version_documento_xsd_id",
            nullable = false
    )
    private VersionDocumentoXsd versionDocumentoXsd;

    @Column(
            name = "ruta_origen",
            nullable = false,
            length = 1000
    )
    private String rutaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_xsd_id")
    private ElementoXsd elementoXsd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_xsd_id")
    private AtributoXsd atributoXsd;

    @Column(
            name = "tipo_mapeo",
            nullable = false,
            length = 20
    )
    private String tipoMapeo;

    @Column(
            name = "estado_registro",
            nullable = false,
            length = 20
    )
    private String estadoRegistro;

    @Column(
            name = "usuario_creacion",
            nullable = false,
            length = 100
    )
    private String usuarioCreacion;

    @Column(
            name = "usuario_modificacion",
            length = 100
    )
    private String usuarioModificacion;

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "observacion", length = 500)
    private String observacion;
}