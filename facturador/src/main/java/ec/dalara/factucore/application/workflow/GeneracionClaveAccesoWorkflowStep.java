package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.service.ClaveAccesoService;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoDatos;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;

import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionClaveAccesoWorkflowStep   {

	private static final String TIPO_EMISION_NORMAL = "1";

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final ClaveAccesoService claveAccesoService;
	private final EmpresaService empresaService;
	private final EmisionService emisionService;
	private final DocumentoDefinitionProvider definitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final SriProperties sriProperties;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.GENERACION_CLAVE_ACCESO;
	}

	/**
	 * Ejecuta la etapa y devuelve exclusivamente su resultado para Camel.
	 * El Bean no conoce ni decide el siguiente nodo del workflow.
	 */
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getSolicitud() == null) {
			throw new ApplicationException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
		}

		boolean claveExistente = contexto.getClaveAcceso() != null && !contexto.getClaveAcceso().isBlank();
		if (claveExistente && !claveAccesoService.validar(contexto.getClaveAcceso())) {
			throw new ApplicationException(MessageCodes.CLAVE_ACCESO_FORMATO_INVALIDO);
		}

		if (contexto.getSecuencial() == null || contexto.getSecuencial().isBlank()) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_NUMERO_REQUERIDO);
		}

		var solicitud = contexto.getSolicitud();

		var empresa = empresaService.obtenerPorId(solicitud.getIdEmpresa())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA));

		var emision = emisionService.resolver(solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(),
				solicitud.getIdPuntoEmision());

		var documento = documentoXsdService.obtenerPorId(solicitud.getIdTipoDocumento())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

		var definition = definitionProvider
				.obtenerDefinicion(solicitud.getIdTipoDocumento(), solicitud.getVersionXsd())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

		String ambiente = resolverAmbiente(sriProperties.getAmbiente());

		contexto.setValorGenerado("ambiente", ambiente);
		contexto.setValorGenerado("tipoEmision", TIPO_EMISION_NORMAL);
		contexto.setValorGenerado("tipoDocumento", documento.getTipoDocumento());
		contexto.setValorGenerado("idDocumentoXml", "comprobante");
		contexto.setValorGenerado("versionDocumento", definition.getVersion().getVersion());

		if (claveExistente) {
			contexto.setValorGenerado("claveAcceso", contexto.getClaveAcceso());
			contexto.setValorGenerado("secuencial", contexto.getSecuencial());
			return ResultadoEtapa.exitosa(EtapaWorkflow.GENERACION_CLAVE_ACCESO, "YA_GENERADA");
		}

		var datos = new ClaveAccesoDatos(LocalDate.from(solicitud.getFechaInicio()),
				definition.getDocumento().getCodigo(), empresa.getRuc(), ambiente,
				emision.establecimiento().getCodigo(), emision.puntoEmision().getCodigo(), contexto.getSecuencial(),
				null, TIPO_EMISION_NORMAL);

		var resultado = claveAccesoService.generarConCodigoNumerico(datos);
		contexto.setClaveAcceso(resultado.getClave());

		contexto.setValorGenerado("claveAcceso", resultado.getClave());
		contexto.setValorGenerado("secuencial", contexto.getSecuencial());

		if (contexto.getComprobante() != null) {
			contexto.getComprobante().setClaveAcceso(resultado.getClave());
		}

		return ResultadoEtapa.exitosa(EtapaWorkflow.GENERACION_CLAVE_ACCESO, "GENERADA");
	}

	private String resolverAmbiente(String ambiente) {
		if ("PRUEBAS".equalsIgnoreCase(ambiente)) {
			return "1";
		}

		if ("PRODUCCION".equalsIgnoreCase(ambiente)) {
			return "2";
		}

		throw new ApplicationException(MessageCodes.SRI_AMBIENTE_REQUERIDO);
	}
}
