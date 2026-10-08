package ec.dalara.factucore.domain.workflow;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.contract.response.ComprobanteGeneracionResponse;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;

public final class ContextoWorkflow {

	private Long comprobanteId;

	private final ComprobanteGeneracionRequest solicitud;

	private final LocalDateTime fechaInicio;

	private Comprobante comprobante;

	private boolean idempotente;

	private String secuencial;

	private String claveAcceso;

	private String xml;

	private DocumentDefinitionModel definicionDocumento;

	private String xmlFirmado;

	private byte[] ride;

	private String numeroAutorizacion;

	private LocalDateTime fechaAutorizacion;

	private String estadoSri;

	private EtapaWorkflow etapaActual;

	private ResultadoEtapa ultimoResultado;

	private final Map<EtapaWorkflow, ResultadoEtapa> resultados = new EnumMap<>(EtapaWorkflow.class);

	private final Map<EtapaWorkflow, EjecucionEtapaWorkflow> ejecuciones = new EnumMap<>(EtapaWorkflow.class);

	private final Map<String, Object> valoresGenerados = new java.util.LinkedHashMap<>();

	private ComprobanteGeneracionResponse respuesta;

	private boolean errorNotificacion;

	private Boolean exitosoFinal;

	private List<ec.dalara.factucore.application.contract.response.MensajeResponse> erroresValidacion = List.of();

	private ec.dalara.factucore.application.validation.ComprobanteValidationResult validacionXsd;

	private ContextoWorkflow(ComprobanteGeneracionRequest solicitud, LocalDateTime fechaInicio) {
		this.solicitud = solicitud;
		this.fechaInicio = fechaInicio;
	}

	public static ContextoWorkflow nuevo(ComprobanteGeneracionRequest solicitud) {
		return new ContextoWorkflow(solicitud, LocalDateTime.now());
	}

	public static ContextoWorkflow existente(Comprobante comprobante, ComprobanteGeneracionRequest solicitud) {
		if (comprobante == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}

		ContextoWorkflow contexto = new ContextoWorkflow(solicitud, comprobante.getFechaCreacion());

		contexto.comprobanteId = comprobante.getId();

		contexto.comprobante = comprobante;

		contexto.secuencial = comprobante.getSecuencial();

		contexto.claveAcceso = comprobante.getClaveAcceso();

		contexto.xml = null;

		contexto.xmlFirmado = null;

		contexto.numeroAutorizacion = comprobante.getNumeroAutorizacion();

		contexto.fechaAutorizacion = comprobante.getFechaAutorizacion();

		return contexto;
	}

	public void marcarIdempotente() {
		this.idempotente = true;
	}

	public boolean isIdempotente() {
		return idempotente;
	}

	public void asignarComprobante(Comprobante comprobante) {
		if (comprobante == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}

		this.comprobante = comprobante;
		this.comprobanteId = comprobante.getId();
	}

	public void setValorGenerado(String clave, Object valor) {
		if (clave != null && !clave.isBlank() && valor != null) {
			valoresGenerados.put(clave, valor);
		}
	}

	public Map<String, Object> getValoresGenerados() {
		return Collections.unmodifiableMap(valoresGenerados);
	}

	/**
	 * Contexto mutable de una ejecución de facturación.
	 *
	 * <p>Conserva los datos generados y el resultado de cada nodo del workflow.
	 * Los Beans de las etapas no conocen la siguiente etapa: registran únicamente
	 * su resultado y devuelven una salida como OK, ERROR, RECIBIDO o AUTORIZADO.
	 * La transición entre nodos es responsabilidad exclusiva del workflow XML
	 * ejecutado por Camel.</p>
	 */
	public void iniciarEtapa(EtapaWorkflow etapa) {
		if (etapa == null) return;
		etapaActual = etapa;
		ejecuciones.put(etapa, new EjecucionEtapaWorkflow(etapa, capturarEntrada(), LocalDateTime.now()));
	}

	private Map<String, Object> capturarEntrada() {
		Map<String, Object> entrada = new LinkedHashMap<>();
		entrada.put("idTransaccion", solicitud == null ? null : solicitud.getIdTransaccion());
		entrada.put("idEmpresa", solicitud == null ? null : solicitud.getIdEmpresa());
		entrada.put("idEstablecimiento", solicitud == null ? null : solicitud.getIdEstablecimiento());
		entrada.put("idPuntoEmision", solicitud == null ? null : solicitud.getIdPuntoEmision());
		entrada.put("idComprobante", comprobanteId);
		entrada.put("secuencial", secuencial);
		entrada.put("claveAcceso", claveAcceso);
		entrada.put("xmlDisponible", xml != null && !xml.isBlank());
		entrada.put("xmlFirmadoDisponible", xmlFirmado != null && !xmlFirmado.isBlank());
		entrada.put("estadoSri", estadoSri);
		entrada.put("idempotente", idempotente);
		return Collections.unmodifiableMap(entrada);
	}

	public void registrarSalidaEtapa(EtapaWorkflow etapa, Object salida) {
		EjecucionEtapaWorkflow ejecucion = ejecuciones.get(etapa);
		if (ejecucion == null) {
			iniciarEtapa(etapa);
			ejecucion = ejecuciones.get(etapa);
		}
		ejecucion.completar(salida, null, LocalDateTime.now());
	}

	public void registrarResultado(ResultadoEtapa resultado) {
		if (resultado == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_RESULTADO_ETAPA_REQUERIDO);
		}

		this.etapaActual = resultado.getEtapa();

		this.ultimoResultado = resultado;

		this.resultados.put(resultado.getEtapa(), resultado);
		EjecucionEtapaWorkflow ejecucion = ejecuciones.get(resultado.getEtapa());
		if (ejecucion == null) { iniciarEtapa(resultado.getEtapa()); ejecucion = ejecuciones.get(resultado.getEtapa()); }
		ejecucion.completar(resultado.salida(), resultado, LocalDateTime.now());
	}

	public String registrarYObtenerSalida(ResultadoEtapa resultado) {
		registrarResultado(resultado);
		return resultado.salida();
	}

	public void setValidacionXsd(ec.dalara.factucore.application.validation.ComprobanteValidationResult validacionXsd) {
		this.validacionXsd = validacionXsd;
	}

	public ec.dalara.factucore.application.validation.ComprobanteValidationResult getValidacionXsd() {
		return validacionXsd;
	}

	public void registrarErroresValidacion(List<ec.dalara.factucore.application.contract.response.MensajeResponse> errores) {
		this.erroresValidacion = errores == null ? List.of() : List.copyOf(errores);
	}

	public List<ec.dalara.factucore.application.contract.response.MensajeResponse> getErroresValidacion() {
		return erroresValidacion;
	}

	public void marcarErrorNotificacion() {
		this.errorNotificacion = true;
	}

	public void setExitosoFinal(boolean exitosoFinal) {
		this.exitosoFinal = exitosoFinal;
	}

	public Boolean getExitosoFinal() {
		return exitosoFinal;
	}

	public boolean isErrorNotificacion() {
		return errorNotificacion;
	}

	public ComprobanteGeneracionResponse getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(ComprobanteGeneracionResponse respuesta) {
		this.respuesta = respuesta;
	}

	public boolean etapaCompletada(EtapaWorkflow etapa) {
		ResultadoEtapa resultado = resultados.get(etapa);

		return resultado != null && resultado.isExitosa();
	}

	public Long getComprobanteId() {
		return comprobanteId;
	}

	public ComprobanteGeneracionRequest getSolicitud() {
		return solicitud;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public Comprobante getComprobante() {
		return comprobante;
	}

	public String getSecuencial() {
		return secuencial;
	}

	public void setSecuencial(String secuencial) {
		this.secuencial = secuencial;
	}

	public String getClaveAcceso() {
		return claveAcceso;
	}

	public void setClaveAcceso(String claveAcceso) {
		this.claveAcceso = claveAcceso;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public DocumentDefinitionModel getDefinicionDocumento() {
		return definicionDocumento;
	}

	public void setDefinicionDocumento(DocumentDefinitionModel definicionDocumento) {
		this.definicionDocumento = definicionDocumento;
	}

	public String getXmlFirmado() {
		return xmlFirmado;
	}

	public void setXmlFirmado(String xmlFirmado) {
		this.xmlFirmado = xmlFirmado;
	}

	public byte[] getRide() {
		return ride;
	}

	public void setRide(byte[] ride) {
		this.ride = ride;
	}

	public String getNumeroAutorizacion() {
		return numeroAutorizacion;
	}

	public void setNumeroAutorizacion(String numeroAutorizacion) {
		this.numeroAutorizacion = numeroAutorizacion;
	}

	public LocalDateTime getFechaAutorizacion() {
		return fechaAutorizacion;
	}

	public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}

	public String getEstadoSri() {
		return estadoSri;
	}

	public void setEstadoSri(String estadoSri) {
		this.estadoSri = estadoSri;
	}

	public EtapaWorkflow getEtapaActual() {
		return etapaActual;
	}

	public ResultadoEtapa getResultado(EtapaWorkflow etapa) { return etapa == null ? null : resultados.get(etapa); }

	public ResultadoEtapa getUltimoResultado() {
		return ultimoResultado;
	}

	public Map<EtapaWorkflow, EjecucionEtapaWorkflow> getEjecuciones() { return Collections.unmodifiableMap(ejecuciones); }

	public EjecucionEtapaWorkflow getEjecucion(EtapaWorkflow etapa) { return ejecuciones.get(etapa); }

	public Map<EtapaWorkflow, ResultadoEtapa> getResultados() {
		return Collections.unmodifiableMap(resultados);
	}
}