package ec.dalara.factucore.adapter.out.persistence;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.FactuCoreSourcePort;
import ec.dalara.factucore.infrastructure.persistence.entity.CatalogoItem;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;

@Component
public class FactuCoreSourceJpaAdapter implements FactuCoreSourcePort {

	private final EntityManager entityManager;

	public FactuCoreSourceJpaAdapter(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional(readOnly = true)
	public ValorOrigen resolver(String origen, Map<String, Object> contexto) {
		OrigenPartes partes = parsear(origen);
		EntityType<?> entidad = buscarEntidad(partes.tabla());

		if (entidad == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.FUENTE.NO_ENCONTRADA", origen);
		}

		if (partes.esCatalogo()) {
			return resolverCatalogoItem(entidad, partes);
		}

		if (esConfiguracionClave(entidad, partes)) {
			return resolverConfiguracion(entidad, partes, contexto);
		}

		SingularAttribute<?, ?> atributo = atributoBasico(entidad, partes.campo());

		if (atributo == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.CAMPO.NO_ENCONTRADO", origen);
		}

		Object id = contexto == null ? null : contexto.get(nombreId(entidad.getJavaType()));

		if (id == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.CONTEXTO.ID.NO_ENCONTRADO", origen);
		}

		String tabla = nombreTabla(entidad.getJavaType());
		String columna = nombreColumna(entidad.getJavaType(), partes.campo());

		String sql = "select e."" + columna + "" from "" + tabla + "" e where e."id" = :id";

		Object resultado = entityManager.createNativeQuery(sql).setParameter("id", id).getResultStream().findFirst()
				.orElse(null);

		return new ValorOrigen(resultado, tipoDato(atributo.getJavaType()));
	}

	private ValorOrigen resolverCatalogoItem(EntityType<?> entidad, OrigenPartes partes) {
		if (!CatalogoItem.class.isAssignableFrom(entidad.getJavaType())) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.FUENTE.NO_ENCONTRADA", partes.origen());
		}

		SingularAttribute<?, ?> atributo = atributoBasico(entidad, partes.campo());

		if (atributo == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.CAMPO.NO_ENCONTRADO", partes.origen());
		}

		String tablaItem = nombreTabla(entidad.getJavaType());
		String columnaItem = nombreColumna(entidad.getJavaType(), partes.campo());
		String columnaCatalogo = nombreColumna(entidad.getJavaType(), "catalogo");
		String tablaCatalogo = "catalogo";

		String sql = "select i."" + columnaItem + "" " + "from "" + tablaItem + "" i "
				+ "join "" + tablaCatalogo + "" c on c."id" = i."" + columnaCatalogo + "" "
				+ "where c."codigo" = :catalogo and c."estado_registro" = 'ACTIVO' "
				+ "and i."estado_registro" = 'ACTIVO' " + "order by i."orden" asc, i."id" asc limit 1";

		Object resultado = entityManager.createNativeQuery(sql).setParameter("catalogo", partes.claveCatalogo())
				.getResultStream().findFirst().orElse(null);

		return new ValorOrigen(resultado, tipoDato(atributo.getJavaType()));
	}

	private ValorOrigen resolverConfiguracion(EntityType<?> entidad, OrigenPartes partes,
			Map<String, Object> contexto) {
		String tabla = nombreTabla(entidad.getJavaType());
		String columnaEmpresa = nombreColumna(entidad.getJavaType(), "empresa");
		String columnaClave = nombreColumna(entidad.getJavaType(), "clave");
		String columnaValor = nombreColumna(entidad.getJavaType(), "valor");

		Object idEmpresa = contexto == null ? null : contexto.get("idEmpresa");

		if (idEmpresa == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.CONTEXTO.ID.NO_ENCONTRADO", partes.origen());
		}

		String sql = "select e."" + columnaValor + "", e."tipo_dato" from "" + tabla + "" e where e.""
				+ columnaEmpresa + "" = :idEmpresa and e."" + columnaClave
				+ "" = :clave and e."estado_registro" = 'ACTIVO' "
				+ "and (e."fecha_vigencia_hasta" is null or e."fecha_vigencia_hasta" >= CURRENT_TIMESTAMP) "
				+ "and e."fecha_vigencia_desde" <= CURRENT_TIMESTAMP "
				+ "order by e."fecha_vigencia_desde" desc limit 1";

		Object fila = entityManager.createNativeQuery(sql).setParameter("idEmpresa", idEmpresa)
				.setParameter("clave", partes.campo()).getResultStream().findFirst().orElse(null);

		if (fila == null) {
			return new ValorOrigen(null, "String");
		}

		if (!(fila instanceof Object[] valores)) {
			return new ValorOrigen(fila, "String");
		}

		return new ValorOrigen(valores[0], valores[1] == null ? "String" : String.valueOf(valores[1]));
	}

	private boolean esConfiguracionClave(EntityType<?> entidad, OrigenPartes partes) {
		return entidad.getJavaType().equals(ConfiguracionEmpresa.class) && partes.esTablaCampo()
				&& atributo(entidad, "clave") != null && atributo(entidad, "valor") != null
				&& atributoBasico(entidad, partes.campo()) == null;
	}

	private SingularAttribute<?, ?> atributoBasico(EntityType<?> entidad, String nombre) {
		Attribute<?, ?> atributo = atributo(entidad, nombre);

		if (atributo instanceof SingularAttribute<?, ?> singular
				&& singular.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
			return singular;
		}

		return null;
	}

	private Attribute<?, ?> atributo(EntityType<?> entidad, String nombre) {
		try {
			return entidad.getAttribute(nombre);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private EntityType<?> buscarEntidad(String tabla) {
		Metamodel metamodel = entityManager.getMetamodel();

		return metamodel.getEntities().stream()
				.filter(entidad -> tabla.equals(nombreTabla(entidad.getJavaType())))
				.findFirst().orElse(null);
	}

	private String nombreTabla(Class<?> tipo) {
		Table table = tipo.getAnnotation(Table.class);

		if (table == null || table.name().isBlank()) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.TABLA.NO_CONFIGURADA", tipo.getSimpleName());
		}

		return table.name();
	}

	private String nombreColumna(Class<?> tipo, String campo) {
		Field field = buscarCampo(tipo, campo);

		if (field == null) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.CAMPO.NO_ENCONTRADO",
					tipo.getSimpleName() + "." + campo);
		}

		Column column = field.getAnnotation(Column.class);

		if (column != null && !column.name().isBlank()) {
			return column.name();
		}

		if (field.getAnnotation(jakarta.persistence.JoinColumn.class) != null) {
			return field.getAnnotation(jakarta.persistence.JoinColumn.class).name();
		}

		return campo;
	}

	private Field buscarCampo(Class<?> tipo, String nombre) {
		Class<?> actual = tipo;

		while (actual != null) {
			try {
				return actual.getDeclaredField(nombre);
			} catch (NoSuchFieldException ignored) {
				actual = actual.getSuperclass();
			}
		}

		return null;
	}

	private String nombreId(Class<?> tipo) {
		return "id" + tipo.getSimpleName();
	}

	private String tipoDato(Class<?> tipo) {
		return tipo == null ? "Object" : tipo.getSimpleName();
	}

	private OrigenPartes parsear(String origen) {
		if (origen == null || origen.isBlank()) {
			throw new ApplicationException("FACTUCORE.MAPEO_XSD.ORIGEN.REQUERIDO");
		}

		String[] partes = origen.split("\.", -1);

		if (partes.length == 2 && !partes[0].isBlank() && !partes[1].isBlank()) {
			return new OrigenPartes(partes[0], null, partes[1], origen);
		}

		if (partes.length == 3 && "catalogo_item".equalsIgnoreCase(partes[0])
				&& !partes[1].isBlank() && !partes[2].isBlank()) {
			return new OrigenPartes(partes[0], partes[1], partes[2], origen);
		}

		throw new ApplicationException("FACTUCORE.MAPEO_XSD.ORIGEN.FORMATO_INVALIDO", origen);
	}

	private record OrigenPartes(String tabla, String claveCatalogo, String campo, String origen) {

		boolean esCatalogo() {
			return claveCatalogo != null;
		}

		boolean esTablaCampo() {
			return claveCatalogo == null;
		}
	}
}
