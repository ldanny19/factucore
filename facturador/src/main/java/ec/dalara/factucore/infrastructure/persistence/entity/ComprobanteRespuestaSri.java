package ec.dalara.factucore.infrastructure.persistence.entity;

import java.time.LocalDateTime;

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
@Table(name = "comprobante_respuesta_sri")
public class ComprobanteRespuestaSri {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_comprobante_respuesta_sri")
    @TableGenerator(
            name = "generador_comprobante_respuesta_sri",
            table = "hibernate_sequences",
            pkColumnName = "sequence_name",
            valueColumnName = "next_val",
            pkColumnValue = "comprobante_respuesta_sri",
            allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "tipo_respuesta", nullable = false, length = 20)
    private String tipoRespuesta;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "identificador", length = 100)
    private String identificador;

    @Column(name = "respuesta", columnDefinition = "jsonb", nullable = false)
    private String respuesta;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

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
