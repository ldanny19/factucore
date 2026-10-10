package ec.dalara.factucore.application.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteSolicitudEnProcesoService {

	private final JdbcTemplate jdbcTemplate;

	public boolean reclamar(Long empresaId, Long idDocumentoOrigen, String idTransaccion) {
		String sql = """
				INSERT INTO comprobante_solicitud_proceso
					(empresa_id, id_documento_origen, id_transaccion, en_proceso, fecha_inicio)
				VALUES (?, ?, ?, TRUE, CURRENT_TIMESTAMP)
				ON CONFLICT (empresa_id, id_documento_origen)
				DO UPDATE SET id_transaccion = EXCLUDED.id_transaccion,
				              en_proceso = TRUE,
				              fecha_inicio = CURRENT_TIMESTAMP
				WHERE comprobante_solicitud_proceso.en_proceso = FALSE
				   OR comprobante_solicitud_proceso.fecha_inicio < CURRENT_TIMESTAMP - INTERVAL '30 minutes'
				RETURNING id_transaccion
				""";
		return Boolean.TRUE.equals(jdbcTemplate.query(sql, ps -> {
			ps.setLong(1, empresaId);
			ps.setLong(2, idDocumentoOrigen);
			ps.setString(3, idTransaccion);
		}, rs -> rs.next()));
	}

	public void liberar(Long empresaId, Long idDocumentoOrigen, String idTransaccion) {
		jdbcTemplate.update("""
				UPDATE comprobante_solicitud_proceso
				   SET en_proceso = FALSE
				 WHERE empresa_id = ?
				   AND id_documento_origen = ?
				   AND id_transaccion = ?
				   AND en_proceso = TRUE
				""", empresaId, idDocumentoOrigen, idTransaccion);
	}
}
