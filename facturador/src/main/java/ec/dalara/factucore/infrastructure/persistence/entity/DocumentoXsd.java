package ec.dalara.factucore.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "documento_xsd")
public class DocumentoXsd implements EstadoRegistroEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_documento_xsd")
	@TableGenerator(name = "generador_documento_xsd", table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val", pkColumnValue = "documento_xsd", allocationSize = 1)
	private Long id;

	@Column(name = "codigo", nullable = false, unique = true, length = 50)
	private String codigo;

	@Column(name = "nombre", nullable = false, length = 300)
	private String nombre;

	@Column(name = "descripcion", length = 500)
	private String descripcion;

	@Column(name = "tipo_documento", nullable = false, length = 50)
	private String tipoDocumento;

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