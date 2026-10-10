package ec.dalara.factucore.application.workflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Persiste las secciones canónicas que tienen representación en el modelo
 * relacional. Se ejecuta en la misma transacción que la cabecera del comprobante.
 */
@Service
@RequiredArgsConstructor
public class PersistenciaSeccionesComprobanteService {

    private final EntityManager entityManager;

    @Transactional
    public void persistir(Comprobante comprobante, JsonNode datos, String usuario) {
        if (comprobante == null || comprobante.getId() == null || datos == null || !datos.isObject()) {
            return;
        }

        JsonNode factura = datos.path("factura");
        if (factura.isMissingNode() || !factura.isObject()) {
            factura = datos;
        }

        Number existentes = (Number) entityManager.createNativeQuery(
                "select (select count(*) from comprobante_detalle where comprobante_id = :id) + "
                + "(select count(*) from comprobante_pago where comprobante_id = :id) + "
                + "(select count(*) from comprobante_informacion_adicional where comprobante_id = :id) + "
                + "(select count(*) from comprobante_retencion where comprobante_id = :id)")
                .setParameter("id", comprobante.getId()).getSingleResult();
        if (existentes.longValue() > 0) {
            return;
        }

        String usuarioAuditoria = usuario == null || usuario.isBlank() ? "FACTUCORE" : usuario;
        LocalDateTime ahora = LocalDateTime.now();

        JsonNode detalles = factura.path("detalles").path("detalle");
        if (detalles.isArray()) {
            int numeroLinea = 1;
            for (JsonNode detalleJson : detalles) {
                ComprobanteDetalle detalle = new ComprobanteDetalle();
                detalle.setComprobante(comprobante);
                detalle.setNumeroLinea(detalleJson.path("numeroLinea").asInt(numeroLinea));
                detalle.setCodigoPrincipal(texto(detalleJson, "codigoPrincipal"));
                detalle.setCodigoAuxiliar(texto(detalleJson, "codigoAuxiliar"));
                detalle.setDescripcion(texto(detalleJson, "descripcion"));
                detalle.setCantidad(decimal(detalleJson, "cantidad"));
                detalle.setPrecioUnitario(decimal(detalleJson, "precioUnitario"));
                detalle.setDescuento(decimal(detalleJson, "descuento"));
                detalle.setPrecioTotalSinImpuesto(decimal(detalleJson, "precioTotalSinImpuesto"));
                auditar(detalle, usuarioAuditoria, ahora);
                entityManager.persist(detalle);
                entityManager.flush();

                JsonNode impuestos = detalleJson.path("impuestos").path("impuesto");
                if (impuestos.isArray()) {
                    for (JsonNode impuestoJson : impuestos) {
                        ComprobanteDetalleImpuesto impuesto = new ComprobanteDetalleImpuesto();
                        impuesto.setComprobanteDetalle(detalle);
                        impuesto.setCodigoImpuesto(texto(impuestoJson, "codigo"));
                        impuesto.setCodigoPorcentaje(texto(impuestoJson, "codigoPorcentaje"));
                        impuesto.setTarifa(decimal(impuestoJson, "tarifa"));
                        impuesto.setBaseImponible(decimal(impuestoJson, "baseImponible"));
                        impuesto.setValor(decimal(impuestoJson, "valor"));
                        impuesto.setValorDevolucionIva(decimal(impuestoJson, "valorDevolucionIva"));
                        auditar(impuesto, usuarioAuditoria, ahora);
                        entityManager.persist(impuesto);
                    }
                }
                persistirInformacionAdicional(comprobante,
                        detalleJson.path("detallesAdicionales").path("detAdicional"), usuarioAuditoria, ahora);
                numeroLinea++;
            }
        }

        JsonNode pagos = factura.path("infoFactura").path("pagos").path("pago");
        if (pagos.isArray()) {
            for (JsonNode pagoJson : pagos) {
                ComprobantePago pago = new ComprobantePago();
                pago.setComprobante(comprobante);
                pago.setCodigoFormaPago(texto(pagoJson, "formaPago"));
                pago.setTotal(decimal(pagoJson, "total"));
                pago.setPlazo(entero(pagoJson, "plazo"));
                pago.setUnidadTiempo(texto(pagoJson, "unidadTiempo"));
                auditar(pago, usuarioAuditoria, ahora);
                entityManager.persist(pago);
            }
        }

        persistirInformacionAdicional(comprobante,
                factura.path("infoAdicional").path("campoAdicional"), usuarioAuditoria, ahora);
        persistirRetenciones(comprobante, factura.path("impuestos").path("impuesto"), usuarioAuditoria, ahora);
    }

    private void persistirRetenciones(Comprobante comprobante, JsonNode nodos, String usuario, LocalDateTime ahora) {
        if (!nodos.isArray()) {
            return;
        }
        for (JsonNode nodo : nodos) {
            ComprobanteRetencion retencion = new ComprobanteRetencion();
            retencion.setComprobante(comprobante);
            retencion.setCodigoImpuesto(texto(nodo, "codigo"));
            retencion.setCodigoRetencion(texto(nodo, "codigoRetencion"));
            retencion.setPorcentajeRetener(decimal(nodo, "porcentajeRetener"));
            retencion.setBaseImponible(decimal(nodo, "baseImponible"));
            retencion.setValorRetenido(decimal(nodo, "valorRetenido"));
            retencion.setNumeroDocumentoSustento(texto(nodo, "numDocSustento"));
            retencion.setFechaEmisionDocumentoSustento(fecha(nodo, "fechaEmisionDocSustento"));
            auditar(retencion, usuario, ahora);
            entityManager.persist(retencion);
        }
    }

    private void persistirInformacionAdicional(Comprobante comprobante, JsonNode nodos, String usuario,
            LocalDateTime ahora) {
        if (!nodos.isArray()) {
            return;
        }
        for (JsonNode nodo : nodos) {
            String nombre = texto(nodo, "nombre");
            if (nombre == null || nombre.isBlank()) {
                continue;
            }
            ComprobanteInformacionAdicional adicional = new ComprobanteInformacionAdicional();
            adicional.setComprobante(comprobante);
            adicional.setNombre(nombre);
            adicional.setValor(texto(nodo, "valor"));
            auditar(adicional, usuario, ahora);
            entityManager.persist(adicional);
        }
    }

    private void auditar(EstadoRegistroEntity entidad, String usuario, LocalDateTime ahora) {
        entidad.setEstadoRegistro("A");
        if (entidad instanceof ComprobanteDetalle e) {
            e.setUsuarioCreacion(usuario); e.setFechaCreacion(ahora);
        } else if (entidad instanceof ComprobanteDetalleImpuesto e) {
            e.setUsuarioCreacion(usuario); e.setFechaCreacion(ahora);
        } else if (entidad instanceof ComprobantePago e) {
            e.setUsuarioCreacion(usuario); e.setFechaCreacion(ahora);
        } else if (entidad instanceof ComprobanteInformacionAdicional e) {
            e.setUsuarioCreacion(usuario); e.setFechaCreacion(ahora);
        } else if (entidad instanceof ComprobanteRetencion e) {
            e.setUsuarioCreacion(usuario); e.setFechaCreacion(ahora);
        }
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() ? null : valor.asText();
    }

    private BigDecimal decimal(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() || valor.asText().isBlank() ? null : valor.decimalValue();
    }

    private Integer entero(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() || valor.asText().isBlank() ? null : valor.asInt();
    }

    private LocalDate fecha(JsonNode nodo, String campo) {
        String valor = texto(nodo, campo);
        return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
    }
}
