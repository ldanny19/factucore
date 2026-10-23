package ec.dalara.factucore.infrastructure.persistence.repository;

import java.time.LocalDate;

public record ComprobanteReporteDiaProjection(LocalDate fecha, long total, long autorizados, long pendientes,
		long noAutorizados, long conError) {
}
