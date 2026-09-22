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
@Table(name = "empresa")
public class Empresa implements EstadoRegistroEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "generador_empresa")
	@TableGenerator(name = "generador_empresa", table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val", pkColumnValue = "empresa", allocationSize = 1)
	private Long id;

	@Column(name = "ruc", nullable = false, unique = true, length = 13)
	private String ruc;

	@Column(name = "razon_social", nullable = false, length = 300)
	private String razonSocial;

	@Column(name = "nombre_comercial", length = 300)
	private String nombreComercial;

	@Column(name = "direccion_matriz", nullable = false, length = 500)
	private String direccionMatriz;

	@Column(name = "obligado_contabilidad", nullable = false)
	private Boolean obligadoContabilidad;

	@Column(name = "contribuyente_rimpe", nullable = false)
	private Boolean contribuyenteRimpe;

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