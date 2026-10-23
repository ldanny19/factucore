package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ComprobanteReporteQueryRepositoryImpl implements ComprobanteReporteQueryRepository {

	private static final List<String> ESTADOS_PENDIENTES = List.of("RECIBIDO", "VALIDADO", "SECUENCIAL_ASIGNADO",
			"CLAVE_ACCESO_GENERADA", "XML_GENERADO", "XSD_VALIDADO", "FIRMADO", "ENVIADO_SRI",
			"AUTORIZACION_PENDIENTE", "AUTORIZACION_SRI", "RIDE_GENERADO");
	private static final List<String> ESTADOS_NO_AUTORIZADOS = List.of("NO_AUTORIZADO", "NO AUTORIZADO",
			"NOAUTORIZADO", "RECHAZADO");

	private final EntityManager entityManager;

	@Override
	public List<ComprobanteReporteDiaProjection> agruparPorDia(
			org.springframework.data.jpa.domain.Specification<Comprobante> criterio) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> consulta = builder.createTupleQuery();
		Root<Comprobante> comprobante = consulta.from(Comprobante.class);
		Predicate filtro = criterio.toPredicate(comprobante, consulta, builder);

		Expression<java.time.LocalDate> fecha = comprobante.get("fechaEmision");
		Expression<String> estadoSri = builder.upper(comprobante.get("estadoSri"));
		Expression<String> estadoProceso = builder.upper(comprobante.get("estadoProceso"));
		Predicate autorizado = builder.equal(estadoSri, "AUTORIZADO");
		Predicate noAutorizado = estadoSri.in(ESTADOS_NO_AUTORIZADOS);
		Predicate sinError = builder.or(builder.isNull(comprobante.get("codigoError")),
				builder.equal(comprobante.get("codigoError"), ""));
		Predicate pendiente = builder.and(sinError, estadoProceso.in(ESTADOS_PENDIENTES),
				builder.not(autorizado), builder.not(noAutorizado));
		Predicate conError = builder.and(builder.isNotNull(comprobante.get("codigoError")),
				builder.notEqual(comprobante.get("codigoError"), ""), builder.not(autorizado),
				builder.not(noAutorizado), builder.not(pendiente));

		consulta.multiselect(fecha.alias("fecha"), builder.count(comprobante).alias("total"),
				contar(builder, autorizado).alias("autorizados"), contar(builder, pendiente).alias("pendientes"),
				contar(builder, noAutorizado).alias("noAutorizados"), contar(builder, conError).alias("conError"));
		consulta.where(filtro);
		consulta.groupBy(fecha);
		consulta.orderBy(builder.asc(fecha));

		return entityManager.createQuery(consulta).getResultList().stream()
				.map(fila -> new ComprobanteReporteDiaProjection(fila.get("fecha", java.time.LocalDate.class),
						fila.get("total", Long.class), fila.get("autorizados", Long.class),
						fila.get("pendientes", Long.class), fila.get("noAutorizados", Long.class),
						fila.get("conError", Long.class)))
				.toList();
	}

	private Expression<Long> contar(CriteriaBuilder builder, Predicate condicion) {
		return builder.sum(builder.<Long>selectCase().when(condicion, 1L).otherwise(0L));
	}
}
