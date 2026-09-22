package ec.dalara.factucore.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
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
@Table(name = "establecimiento")
public class Establecimiento implements EstadoRegistroEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_establecimiento")
	@TableGenerator(name = "generador_establecimiento", table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val", pkColumnValue = "establecimiento", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Column(name = "codigo", nullable = false, length = 3)
	private String codigo;

	@Column(name = "nombre", length = 300)
	private String nombre;

	@Column(name = "direccion", nullable = false, length = 500)
	private String direccion;

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