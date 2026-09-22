package ec.dalara.factucore.application.workflow;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.service.ClaveAccesoService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoDatos;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneracionClaveAccesoWorkflowStep implements WorkflowStep {

    private static final String TIPO_EMISION_NORMAL = "1";

    private final ClaveAccesoService claveAccesoService;
    private final EmpresaService empresaService;
    private final EmisionService emisionService;
    private final DocumentoDefinitionProvider definitionProvider;
    private final SriProperties sriProperties;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.GENERACION_CLAVE_ACCESO;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getSolicitud() == null) {
            throw new WorkflowException(MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO);
        }

        if (contexto.getClaveAcceso() != null && !contexto.getClaveAcceso().isBlank()) {
            if (!claveAccesoService.validar(contexto.getClaveAcceso())) {
                throw new WorkflowException(MessageCodes.CLAVE_ACCESO_FORMATO_INVALIDO);
            }
            return ResultadoEtapa.exitosa(
                    EtapaWorkflow.GENERACION_CLAVE_ACCESO,
                    "YA_GENERADA");
        }

        if (contexto.getSecuencial() == null || contexto.getSecuencial().isBlank()) {
            throw new WorkflowException(MessageCodes.COMPROBANTE_NUMERO_REQUERIDO);
        }

        var solicitud = contexto.getSolicitud();

        var empresa = empresaService.obtenerPorId(solicitud.getIdEmpresa())
                .orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA));

        var emision = emisionService.resolver(
                solicitud.getIdEmpresa(),
                solicitud.getCodigoEstablecimiento(),
                solicitud.getPuntoEmision());

        var definition = definitionProvider
                .obtenerDefinicionVigente(
                        solicitud.getTipoDocumento(),
                        solicitud.getFechaInicio().toLocalDateTime())
                .orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

        String ambiente = resolverAmbiente(sriProperties.getAmbiente());

        var datos = new ClaveAccesoDatos(
                LocalDate.from(solicitud.getFechaInicio()),
                definition.getDocumento().getCodigo(),
                empresa.getRuc(),
                ambiente,
                emision.establecimiento().getCodigo(),
                emision.puntoEmision().getCodigo(),
                contexto.getSecuencial(),
                null,
                TIPO_EMISION_NORMAL);

        var resultado = claveAccesoService.generarConCodigoNumerico(datos);
        contexto.setClaveAcceso(resultado.getClave());

        if (contexto.getComprobante() != null) {
            contexto.getComprobante().setClaveAcceso(resultado.getClave());
        }

        return ResultadoEtapa.exitosa(
                EtapaWorkflow.GENERACION_CLAVE_ACCESO,
                "GENERADA");
    }

    private String resolverAmbiente(String ambiente) {
        if ("PRUEBAS".equalsIgnoreCase(ambiente)) {
            return "1";
        }

        if ("PRODUCCION".equalsIgnoreCase(ambiente)) {
            return "2";
        }

        throw new WorkflowException(MessageCodes.SRI_AMBIENTE_REQUERIDO);
    }
}
