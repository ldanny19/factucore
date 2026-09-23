package ec.dalara.factucore.application.workflow;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.application.service.VersionDocumentoXsdService;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersistenciaComprobanteWorkflowStep implements WorkflowStep {

    private final ComprobanteRepository comprobanteRepository;
    private final EmpresaService empresaService;
    private final EmisionService emisionService;
    private final DocumentoDefinitionProvider definitionProvider;
    private final DocumentoXsdService documentoXsdService;
    private final VersionDocumentoXsdService versionDocumentoXsdService;
    private final SriProperties sriProperties;
    private final ObjectMapper objectMapper;

    @Override
    public EtapaWorkflow etapa() {
        return EtapaWorkflow.RECEPCION;
    }

    @Override
    public ResultadoEtapa ejecutar(ContextoWorkflow contexto) {
        if (contexto == null || contexto.getSolicitud() == null) {
            throw new WorkflowException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
        }

        var solicitud = contexto.getSolicitud();
        var empresa = empresaService.obtenerPorId(solicitud.getIdEmpresa())
                .orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA));
        var emision = emisionService.resolver(
                solicitud.getIdEmpresa(), solicitud.getCodigoEstablecimiento(), solicitud.getPuntoEmision());

        var definition = definitionProvider.obtenerDefinicionVigente(
                solicitud.getTipoDocumento(), solicitud.getFechaInicio().toLocalDateTime())
                .orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

        contexto.setDefinicionDocumento(definition);

        var documentoXsd = documentoXsdService.obtenerPorCodigo(definition.getDocumento().getCodigo())
                .orElseThrow(() -> new WorkflowException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));
        var versionXsd = versionDocumentoXsdService.obtenerPorId(definition.getVersion().getId())
                .orElseThrow(() -> new WorkflowException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));

        if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
            throw new WorkflowException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
        }

        var existente = comprobanteRepository.findByClaveAcceso(contexto.getClaveAcceso())
                .filter(c -> !EstadoRegistro.ELIMINADO.equals(c.getEstadoRegistro()));

        if (existente.isPresent()) {
            contexto.asignarComprobante(existente.get());
            return ResultadoEtapa.exitosa(etapa(), "YA_REGISTRADO");
        }

        Comprobante comprobante = Comprobante.builder()
                .empresa(empresa)
                .establecimiento(emision.establecimiento())
                .puntoEmision(emision.puntoEmision())
                .documentoXsd(documentoXsd)
                .versionDocumentoXsd(versionXsd)
                .ambiente(resolverAmbiente(sriProperties.getAmbiente()))
                .tipoEmision("1")
                .codigoDocumento(definition.getDocumento().getCodigo())
                .secuencial(contexto.getSecuencial())
                .claveAcceso(contexto.getClaveAcceso())
                .fechaEmision(solicitud.getFechaInicio().toLocalDate())
                .razonSocialEmisor(empresa.getRazonSocial())
                .nombreComercialEmisor(empresa.getNombreComercial())
                .rucEmisor(empresa.getRuc())
                .direccionMatrizEmisor(empresa.getDireccionMatriz())
                .direccionEstablecimientoEmisor(emision.establecimiento().getDireccion())
                .estadoProceso(EstadoProceso.CLAVE_ACCESO_GENERADA.name())
                .datosComprobante(serializarDatos(solicitud))
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .usuarioCreacion(solicitud.getUsuario())
                .fechaCreacion(LocalDateTime.now())
                .build();

        comprobante = comprobanteRepository.save(comprobante);
        contexto.asignarComprobante(comprobante);

        return ResultadoEtapa.exitosa(etapa(), "REGISTRADO");
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

    private String serializarDatos(ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest solicitud) {
        try {
            var datos = solicitud.getDatos() == null ? new LinkedHashMap<>() :
                    solicitud.getDatos().stream().collect(Collectors.toMap(
                            item -> item.getKey(), item -> item.getValue(),
                            (primero, segundo) -> segundo, LinkedHashMap::new));
            return objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException exception) {
            throw new WorkflowException(MessageCodes.COMPROBANTE_DATOS_REQUERIDOS);
        }
    }
}
