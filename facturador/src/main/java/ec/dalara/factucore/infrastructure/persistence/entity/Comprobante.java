package ec.dalara.factucore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comprobante")
public class Comprobante implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante")
    @TableGenerator(
        name = "generador_comprobante",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "comprobante",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_emision_id", nullable = false)
    private PuntoEmision puntoEmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_xsd_id", nullable = false)
    private DocumentoXsd documentoXsd;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_documento_xsd_id", nullable = false)
    private VersionDocumentoXsd versionDocumentoXsd;

    @Column(name = "ambiente", nullable = false, length = 1)
    private String ambiente;

    @Column(name = "tipo_emision", nullable = false, length = 1)
    private String tipoEmision;

    @Column(name = "codigo_documento", nullable = false, length = 2)
    private String codigoDocumento;

    @Column(name = "secuencial", nullable = false, length = 9)
    private String secuencial;

    @Column(name = "clave_acceso", nullable = false, unique = true, length = 49)
    private String claveAcceso;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "razon_social_emisor", nullable = false, length = 300)
    private String razonSocialEmisor;

    @Column(name = "nombre_comercial_emisor", length = 300)
    private String nombreComercialEmisor;

    @Column(name = "ruc_emisor", nullable = false, length = 13)
    private String rucEmisor;

    @Column(name = "direccion_matriz_emisor", nullable = false, length = 500)
    private String direccionMatrizEmisor;

    @Column(name = "direccion_establecimiento_emisor", length = 500)
    private String direccionEstablecimientoEmisor;

    @Column(name = "identificacion_receptor", length = 50)
    private String identificacionReceptor;

    @Column(name = "tipo_identificacion_receptor", length = 10)
    private String tipoIdentificacionReceptor;

    @Column(name = "razon_social_receptor", length = 300)
    private String razonSocialReceptor;

    @Column(name = "direccion_receptor", length = 500)
    private String direccionReceptor;

    @Column(name = "estado_proceso", nullable = false, length = 50)
    private String estadoProceso;

    @Column(name = "codigo_error", length = 100)
    private String codigoError;

    @Column(name = "mensaje_error", length = 2000)
    private String mensajeError;

    @Column(name = "numero_autorizacion", length = 100)
    private String numeroAutorizacion;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Column(name = "ruta_xml_firmado", length = 1000)
    private String rutaXmlFirmado;

    @Column(name = "ruta_respuesta_sri", length = 1000)
    private String rutaRespuestaSri;

    @Column(name = "ruta_ride", length = 1000)
    private String rutaRide;

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