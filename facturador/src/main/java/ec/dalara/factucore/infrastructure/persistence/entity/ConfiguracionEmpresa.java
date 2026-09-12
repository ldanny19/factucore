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
@Table(
    name = "configuracion_empresa",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_configuracion_empresa",
            columnNames = {
                "empresa_id",
                "clave",
                "fecha_vigencia_desde"
            }
        )
    }
)
public class ConfiguracionEmpresa implements EstadoRegistroEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_configuracion_empresa")
    @TableGenerator(
        name = "generador_configuracion_empresa",
        table = "hibernate_sequences",
        pkColumnName = "sequence_name",
        valueColumnName = "next_val",
        pkColumnValue = "configuracion_empresa",
        allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    @Column(name = "valor", length = 2000)
    private String valor;

    @Column(name = "tipo_dato", nullable = false, length = 30)
    private String tipoDato;

    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDateTime fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDateTime fechaVigenciaHasta;

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