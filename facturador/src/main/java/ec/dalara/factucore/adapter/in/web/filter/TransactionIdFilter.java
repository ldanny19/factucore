package ec.dalara.factucore.adapter.in.web.filter;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionIdFilter extends OncePerRequestFilter {

	public static final String ATTRIBUTE_ID_TRANSACCION = "factucore.idTransaccion";
	public static final String MDC_ID_TRANSACCION = "idTransaccion";

	private final JdbcTemplate jdbcTemplate;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String method = request.getMethod().toUpperCase(Locale.ROOT);
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();

		boolean apiFactuCore = uri.startsWith(contextPath + "/api/v1/");
		boolean metodoModificacion = "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
		boolean json = request.getContentType() != null
				&& request.getContentType().toLowerCase(Locale.ROOT).contains("application/json");

		return !(apiFactuCore && metodoModificacion && json);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Long secuencia = jdbcTemplate.queryForObject("SELECT nextval('secuencia_id_transaccion')", Long.class);
		if (secuencia == null) {
			throw new ServletException("No fue posible generar idTransaccion.");
		}

		String idTransaccion = secuencia.toString();
		request.setAttribute(ATTRIBUTE_ID_TRANSACCION, idTransaccion);
		MDC.put(MDC_ID_TRANSACCION, idTransaccion);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_ID_TRANSACCION);
		}
	}
}
