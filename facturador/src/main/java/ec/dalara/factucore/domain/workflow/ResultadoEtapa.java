package ec.dalara.factucore.domain.workflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resultado interno producido por una etapa del workflow.
 *
 * <p>Se conserva como objeto de dominio para auditoría, persistencia y
 * diagnóstico. La interfaz que Camel consume es {@link #salida()}, que evita
 * acoplar el Bean con el nombre del siguiente nodo.</p>
 */
public final class ResultadoEtapa {

	private final EtapaWorkflow etapa;
	private final boolean exitosa;
	private final String estado;
	private final String codigoError;
	private final String mensaje;
	private final Map<String, Object> datos;

	private ResultadoEtapa(EtapaWorkflow etapa, boolean exitosa, String estado, String codigoError, String mensaje,
			Map<String, Object> datos) {
		this.etapa = etapa;
		this.exitosa = exitosa;
		this.estado = estado;
		this.codigoError = codigoError;
		this.mensaje = mensaje;
		this.datos = datos == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(datos));
	}

	public static ResultadoEtapa exitosa(EtapaWorkflow etapa, String estado) {
		return new ResultadoEtapa(etapa, true, estado, null, null, Map.of());
	}

	public static ResultadoEtapa exitosa(EtapaWorkflow etapa, String estado, Map<String, Object> datos) {
		return new ResultadoEtapa(etapa, true, estado, null, null, datos);
	}

	public static ResultadoEtapa fallida(EtapaWorkflow etapa, String estado, String codigoError, String mensaje) {
		return fallida(etapa, estado, codigoError, mensaje, Map.of());
	}

	public static ResultadoEtapa fallida(EtapaWorkflow etapa, String estado, String codigoError, String mensaje,
			Map<String, Object> datos) {
		return new ResultadoEtapa(etapa, false, estado, codigoError, mensaje, datos);
	}

	/**
	 * Convierte el resultado interno de una etapa en el valor que consume el
	 * workflow definido en Camel.
	 *
	 * <p>El Bean nunca conoce el siguiente nodo. Solo informa el resultado de su
	 * propia ejecución. Camel utiliza este valor para resolver la transición
	 * declarada en el XML.</p>
	 */
	public String salida() {
		if (etapa == EtapaWorkflow.NOTIFICACION && "NOTIFICACION_ERROR".equals(estado)) {
			return "ERROR";
		}
		if (etapa == EtapaWorkflow.FIN) {
			return "FIN";
		}
		if (etapa == EtapaWorkflow.ENVIO_SRI && "RECHAZADO".equals(estado)) {
			return "RECHAZADO";
		}
		if (!exitosa) {
			return "ERROR";
		}
		if (etapa == EtapaWorkflow.ENVIO_SRI && "ENVIADO_SRI".equals(estado)) {
			return "RECIBIDO";
		}
		if (etapa == EtapaWorkflow.AUTORIZACION_SRI) {
			if ("AUTORIZADO".equals(estado)) {
				return "AUTORIZADO";
			}
			if ("AUTORIZACION_PENDIENTE".equals(estado)) {
				return "EN_PROCESO";
			}
			return "NO_AUTORIZADO";
		}
		return "OK";
	}

	public EtapaWorkflow getEtapa() {
		return etapa;
	}

	public boolean isExitosa() {
		return exitosa;
	}

	public String getEstado() {
		return estado;
	}

	public String getCodigoError() {
		return codigoError;
	}

	public String getMensaje() {
		return mensaje;
	}

	public Map<String, Object> getDatos() {
		return datos;
	}
}
