package ec.dalara.factucore.application.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ec.dalara.factucore.application.service.ComprobanteAuditoriaService;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowResultadoService {

    private final ComprobanteAuditoriaService comprobanteAuditoriaService;
    private final ComprobanteService comprobanteService;
    private final PersistenciaSeccionesComprobanteService persistenciaSeccionesComprobanteService;

    @Transactional
    public String registrar(ContextoWorkflow contexto, ResultadoEtapa resultado) {
        contexto.registrarResultado(resultado);
        persistir(contexto, resultado);
        return resultado.salida();
    }


    private void calcularDatosPersistidos(Comprobante comprobante, JsonNode datos) {
        if (datos == null || !datos.isObject()) return;
        JsonNode datosDocumento = datos.has("datos") && datos.path("datos").isObject()
                ? datos.path("datos") : datos;
        JsonNode factura = datosDocumento.path("comprobante").path("factura");
        if (!factura.isObject()) {
            factura = datosDocumento.path("factura");
        }
        if (!factura.isObject()) return;
        JsonNode infoFactura = factura.path("infoFactura");
        String razonSocial = texto(infoFactura, "razonSocialComprador");
        String identificacion = texto(infoFactura, "identificacionComprador");
        String direccion = texto(infoFactura, "direccionComprador");
        if (razonSocial != null) comprobante.setRazonSocialReceptor(razonSocial);
        if (identificacion != null) comprobante.setIdentificacionReceptor(identificacion);
        if (direccion != null) comprobante.setDireccionReceptor(direccion);
        comprobante.setCorreoComprador(resolverCorreo(datos, factura));

        JsonNode detalles = factura.path("detalles").path("detalle");
        if (!detalles.isArray()) return;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal sinImpuestos = BigDecimal.ZERO;
        BigDecimal impuestos = BigDecimal.ZERO;
        boolean hayTotalLinea = false;
        for (JsonNode detalle : detalles) {
            BigDecimal cantidad = decimal(detalle, "cantidad");
            BigDecimal precio = decimal(detalle, "precioUnitario");
            BigDecimal desc = cero(decimal(detalle, "descuento"));
            BigDecimal totalLinea = decimal(detalle, "precioTotalSinImpuesto");
            if (cantidad != null && precio != null) subtotal = subtotal.add(cantidad.multiply(precio));
            else if (totalLinea != null) subtotal = subtotal.add(totalLinea).add(desc);
            descuento = descuento.add(desc);
            if (totalLinea != null) {
                sinImpuestos = sinImpuestos.add(totalLinea);
                hayTotalLinea = true;
            }
            JsonNode lineTaxes = detalle.path("impuestos").path("impuesto");
            if (lineTaxes.isArray()) for (JsonNode tax : lineTaxes) impuestos = impuestos.add(cero(decimal(tax, "valor")));
        }
        if (!hayTotalLinea) sinImpuestos = subtotal.subtract(descuento);
        JsonNode totalTaxes = infoFactura.path("totalConImpuestos").path("totalImpuesto");
        if (totalTaxes.isArray()) {
            impuestos = BigDecimal.ZERO;
            for (JsonNode tax : totalTaxes) impuestos = impuestos.add(cero(decimal(tax, "valor")));
        }
        BigDecimal propina = cero(decimal(infoFactura, "propina"));
        comprobante.setSubtotal(redondear(subtotal));
        comprobante.setTotalDescuento(redondear(descuento));
        comprobante.setTotalSinImpuestos(redondear(sinImpuestos));
        comprobante.setTotalImpuestos(redondear(impuestos));
        comprobante.setTotalPropina(redondear(propina));
        comprobante.setTotalAPagar(redondear(sinImpuestos.add(impuestos).add(propina)));
    }

    private String resolverCorreo(JsonNode datos, JsonNode factura) {
        JsonNode datosDocumento = datos != null && datos.has("datos") && datos.path("datos").isObject()
                ? datos.path("datos") : datos;
        String externo = texto(datosDocumento, "correo");
        if (externo != null && !externo.isBlank()) return externo.trim();
        JsonNode campos = factura.path("infoAdicional").path("campoAdicional");
        if (campos.isArray()) for (JsonNode campo : campos) {
            String nombre = texto(campo, "nombre");
            String valor = texto(campo, "valor");
            if ("Correo".equalsIgnoreCase(nombre) && valor != null && !valor.isBlank()) return valor.trim();
        }
        return null;
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() || valor.isContainerNode() ? null : valor.asText();
    }

    private BigDecimal decimal(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() || valor.isContainerNode() || valor.asText().isBlank()
                ? null : valor.decimalValue();
    }

    private BigDecimal cero(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor; }
    private BigDecimal redondear(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }

    private void persistir(ContextoWorkflow contexto, ResultadoEtapa resultado) {
        String estadoAnterior = contexto.getComprobante() == null ? null
                : contexto.getComprobante().getEstadoProceso();
        LocalDateTime fechaInicio = LocalDateTime.now();

        if (contexto.getComprobante() != null) {
            if (resultado.isExitosa() && contexto.getSolicitud() != null) {
                calcularDatosPersistidos(contexto.getComprobante(), contexto.getSolicitud().getDatos());
            }
            if (!resultado.isExitosa()) {
                contexto.getComprobante().setCodigoError(resultado.getCodigoError());
                contexto.getComprobante().setMensajeError(resultado.getMensaje());
            } else {
                contexto.getComprobante().setCodigoError(null);
                contexto.getComprobante().setMensajeError(null);
            }
            contexto.asignarComprobante(comprobanteService.guardar(contexto.getComprobante()));

            if (resultado.isExitosa() && contexto.getSolicitud() != null) {
                JsonNode datos = contexto.getSolicitud().getDatos();
                persistenciaSeccionesComprobanteService.persistir(contexto.getComprobante(), datos,
                        contexto.getSolicitud().getUsuario());
            }
        }

        if (contexto.getComprobanteId() != null && resultado.getEtapa() != null) {
            comprobanteAuditoriaService.registrarResultado(contexto.getComprobanteId(), resultado.getEtapa().name(),
                    estadoAnterior, resultado, fechaInicio, LocalDateTime.now());
        }
    }
}
