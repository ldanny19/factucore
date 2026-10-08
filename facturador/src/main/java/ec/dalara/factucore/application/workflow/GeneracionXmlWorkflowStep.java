package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowResultadoService;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.XmlGeneratorPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;

import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionXmlWorkflowStep  {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneracionXmlWorkflowStep.class);

	private final WorkflowResultadoService workflowResultadoService;
	private final MessageResolver messageResolver;
	private final DocumentoDefinitionProvider definitionProvider;
	private final XmlGeneratorPort xmlGenerator;
	private final ObjectMapper objectMapper;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERACION_XML;
	}

	/**
	 * Ejecuta la etapa y devuelve exclusivamente su resultado para Camel.
	 * El Bean no conoce ni decide el siguiente nodo del workflow.
	 */
	public String ejecutar(ContextoWorkflow contexto) {
		String idTransaccion = contexto != null && contexto.getSolicitud() != null
				? contexto.getSolicitud().getIdTransaccion()
				: null;
		LOGGER.debug("ID_TRANSACCION={} - Inicia Etapa {}", idTransaccion, etapa());
		try {
			ResultadoEtapa resultado = ejecutarResultado(contexto);
			String salida = workflowResultadoService.registrar(contexto, resultado);
			LOGGER.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		} catch (RuntimeException exception) {
			String codigo = exception instanceof ApplicationException applicationException
					? applicationException.getCodigo()
					: MessageCodes.WORKFLOW_ETAPA_ERROR;
			Object[] parametros = exception instanceof ApplicationException applicationException
					? applicationException.getParametros()
					: new Object[] { etapa().name() };
			String mensaje = messageResolver.resolver(codigo, parametros);
			ResultadoEtapa resultadoError = ResultadoEtapa.fallida(etapa(), EstadoProceso.ERROR.name(), codigo, mensaje);
			if (contexto != null) {
				workflowResultadoService.registrar(contexto, resultadoError);;
			}
			LOGGER.error("ID_TRANSACCION={} - Error Etapa {} - codigo={} - mensaje={}", idTransaccion, etapa(), codigo, mensaje,
					exception);
			String salida = resultadoError.salida();
			LOGGER.debug("ID_TRANSACCION={} - Fin Etapa {} - Resultado={}", idTransaccion, etapa(), salida);
			return salida;
		}
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		var solicitud = contexto.getSolicitud();
		var definition = contexto.getDefinicionDocumento();

		if (definition == null) {
			definition = definitionProvider
					.obtenerDefinicion(solicitud.getIdTipoDocumento(), solicitud.getVersionXsd())
					.orElseThrow(() -> new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));
			contexto.setDefinicionDocumento(definition);
		}

		var datos = solicitud.getDatos() == null || solicitud.getDatos().isNull() || !solicitud.getDatos().isObject()
				? new LinkedHashMap<String, Object>()
				: objectMapper.convertValue(solicitud.getDatos(),
						new TypeReference<LinkedHashMap<String, Object>>() {
						});

		if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new ApplicationException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		}

		Map<String, Object> contextoFactuCore = new LinkedHashMap<>();
		contextoFactuCore.put("idEmpresa", solicitud.getIdEmpresa());
		contextoFactuCore.put("idEstablecimiento", solicitud.getIdEstablecimiento());
		contextoFactuCore.put("idPuntoEmision", solicitud.getIdPuntoEmision());
		contextoFactuCore.put("idTipoDocumento", solicitud.getIdTipoDocumento());
		contextoFactuCore.put("idTransaccion", solicitud.getIdTransaccion());
		contextoFactuCore.put("versionXsd", solicitud.getVersionXsd());
		contextoFactuCore.put("usuario", solicitud.getUsuario());
		contextoFactuCore.put("canal", solicitud.getCanal());

		Map<String, Object> contextoGenerado = new LinkedHashMap<>(contexto.getValoresGenerados());
		contextoGenerado.put("claveAcceso", contexto.getClaveAcceso());

		if (contexto.getSecuencial() != null && !contexto.getSecuencial().isBlank()) {
			contextoGenerado.put("secuencial", contexto.getSecuencial());
		}

		String xml = xmlGenerator.generar(
				definition,
				datos,
				contextoFactuCore,
				contextoGenerado);
		contexto.setXml(xml);

		LOGGER.info("XML generado antes de validacion XSD:\n{}", xml);

		return ResultadoEtapa.exitosa(etapa(), "COMPLETADA", java.util.Map.of("xmlGenerado", true));
	}
}
