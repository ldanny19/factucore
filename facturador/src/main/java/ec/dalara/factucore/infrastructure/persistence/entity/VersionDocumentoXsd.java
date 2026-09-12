package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "version_documento_xsd")
public class VersionDocumentoXsd {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_version_documento_xsd")
    @TableGenerator(
        name = "generador_version_documento_xsd",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "version_documento_xsd",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_xsd_id", nullable = false)
    private DocumentoXsd documentoXsd;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "nombre_archivo", length = 300)
    private String nombreArchivo;

    @Column(name = "ruta_xsd", length = 1000)
    private String rutaXsd;

    @Column(name = "namespace_xml", length = 1000)
    private String namespaceXml;

    @Column(name = "elemento_raiz", length = 300)
    private String elementoRaiz;
    
    @Column(name = "plantilla_json", columnDefinition = "TEXT")
    private String plantillaJson;

    @Column(name = "esquema_json", columnDefinition = "TEXT")
    private String esquemaJson;

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