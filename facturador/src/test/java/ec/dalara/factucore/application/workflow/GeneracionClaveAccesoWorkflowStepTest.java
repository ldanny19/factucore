package ec.dalara.factucore.application.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.service.ClaveAccesoService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoDatos;
import ec.dalara.factucore.domain.claveacceso.ClaveAccesoModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentoXsdModel;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Empresa;
import ec.dalara.factucore.infrastructure.persistence.entity.Establecimiento;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;

class GeneracionClaveAccesoWorkflowStepTest {

    @Test
    void generaClaveDespuesDeAsignarSecuencial() {
        var claveAccesoService = mock(ClaveAccesoService.class);
        var empresaService = mock(EmpresaService.class);
        var emisionService = mock(EmisionService.class);
        var definitionProvider = mock(DocumentoDefinitionProvider.class);

        var sriProperties = new SriProperties();
        sriProperties.setAmbiente("PRUEBAS");

        var empresa = Empresa.builder().id(10L).ruc("0999999999001").build();
        var establecimiento = Establecimiento.builder().codigo("001").build();
        var puntoEmision = PuntoEmision.builder().codigo("001").build();

        when(empresaService.obtenerPorId(10L)).thenReturn(Optional.of(empresa));
        when(emisionService.resolver(10L, "001", "001"))
                .thenReturn(new EmisionService.ResultadoEmision(establecimiento, puntoEmision));

        var documento = new DocumentoXsdModel("01", "Factura", null, "FACTURA");
        var definition = mock(DocumentDefinitionModel.class);
        when(definition.getDocumento()).thenReturn(documento);
        when(definitionProvider.obtenerDefinicionVigente(any(), any())).thenReturn(Optional.of(definition));

        var clave = mock(ClaveAccesoModel.class);
        when(clave.getClave()).thenReturn("1234567890123456789012345678901234567890123456789");
        when(claveAccesoService.generarConCodigoNumerico(any(ClaveAccesoDatos.class))).thenReturn(clave);

        var request = ComprobanteGeneracionRequest.builder()
                .idTransaccion("tx-1")
                .fechaInicio(OffsetDateTime.parse("2026-09-22T10:00:00-05:00"))
                .usuario("usuario")
                .canal("API")
                .idEmpresa(10L)
                .codigoEstablecimiento("001")
                .puntoEmision("001")
                .tipoDocumento("FACTURA")
                .build();

        var contexto = ContextoWorkflow.nuevo(request);
        contexto.setSecuencial("000000001");

        var step = new GeneracionClaveAccesoWorkflowStep(
                claveAccesoService,
                empresaService,
                emisionService,
                definitionProvider,
                sriProperties);

        var resultado = step.ejecutar(contexto);

        assertTrue(resultado.isExitosa());
        assertEquals("1234567890123456789012345678901234567890123456789", contexto.getClaveAcceso());
        assertEquals("GENERADA", resultado.getEstado());
    }
}
