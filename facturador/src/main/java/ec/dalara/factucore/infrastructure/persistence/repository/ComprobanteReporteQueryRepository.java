package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public interface ComprobanteReporteQueryRepository {

	List<ComprobanteReporteDiaProjection> agruparPorDia(Specification<Comprobante> criterio);
}
