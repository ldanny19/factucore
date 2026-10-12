package ec.dalara.factucore.application.workflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteRetencion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Persiste las secciones canónicas que tienen representación en el modelo
 * relacional. Se ejecuta en la misma transacción que la cabecera del comprobante.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistenciaSeccionesComprobanteService {

    private final EntityManager entityManager;

    @Transactional
    public void persistir(Comprobante comprobante, JsonNode datos, String usuario) {
        if (comprobante == null || comprobante.getId() == null || datos == null || !datos.isObject()) {
            return;
        }

        JsonNode datosDocumento = datos.has("datos") && datos.path("datos").isObject()
                ? datos.path("datos") : datos;
        JsonNode factura = datosDocumento.path("comprobante").path("factura");
        if (!factura.isObject()) {
            factura = datosDocumento.path("factura");
        }
        if (!factura.isObject()) {
            return;
        }

        /*
         * Detalles, impuestos y pagos se sincronizan por clave funcional para
         * completar registros existentes y mantener la operación idempotente.
         * Las secciones auxiliares conservan la protección contra duplicados.
         */
        boolean persistirInformacionAdicional = !existenRegistros(
                "comprobante_informacion_adicional", comprobante.getId());
        boolean persistirRetenciones = !existenRegistros("comprobante_retencion", comprobante.getId());

        String usuarioAuditoria = usuario == null || usuario.isBlank() ? "FACTUCORE" : usuario;
        LocalDateTime ahora = LocalDateTime.now();

        JsonNode detalles = factura.path("detalles").path("detalle");
        if (detalles.isArray()) {
            int numeroLinea = 1;
            for (JsonNode detalleJson : detalles) {
                int linea = detalleJson.path("numeroLinea").asInt(numeroLinea);
                if (linea <= 0) {
                    linea = numeroLinea;
                }

                ComprobanteDetalle detalle = buscarDetalle(comprobante.getId(), linea);
                boolean nuevo = detalle == null;
                if (nuevo) {
                    detalle = new ComprobanteDetalle();
                    detalle.setComprobante(comprobante);
                    detalle.setNumeroLinea(linea);
                }

                detalle.setCodigoPrincipal(texto(detalleJson, "codigoPrincipal"));
                detalle.setCodigoAuxiliar(texto(detalleJson, "codigoAuxiliar"));
                detalle.setDescripcion(texto(detalleJson, "descripcion"));
                detalle.setCantidad(decimal(detalleJson, "cantidad"));
                detalle.setPrecioUnitario(decimal(detalleJson, "precioUnitario"));
                detalle.setDescuento(decimal(detalleJson, "descuento"));
                detalle.setPrecioTotalSinImpuesto(decimal(detalleJson, "precioTotalSinImpuesto"));
                log.debug("ID_TRANSACCION={} - Detalle línea {} antes de persistir: cantidad={}, precioUnitario={}, descuento={}, precioTotalSinImpuesto={}",
                        comprobante.getIdTransaccion(), linea, detalle.getCantidad(), detalle.getPrecioUnitario(),
                        detalle.getDescuento(), detalle.getPrecioTotalSinImpuesto());
                if (nuevo) {
                    auditar(detalle, usuarioAuditoria, ahora);
                    entityManager.persist(detalle);
                } else {
                    auditarModificacion(detalle, usuarioAuditoria, ahora);
                    detalle.setEstadoRegistro(EstadoRegistro.ACTIVO);
                }
                entityManager.flush();

                JsonNode impuestos = detalleJson.path("impuestos").path("impuesto");
                if (impuestos.isArray()) {
                    sincronizarImpuestos(detalle, impuestos, usuarioAuditoria, ahora);
                }

                if (persistirInformacionAdicional) {
                    persistirInformacionAdicional(comprobante,
                            detalleJson.path("detallesAdicionales").path("detAdicional"), usuarioAuditoria, ahora);
                }
                numeroLinea++;
            }
        }

        JsonNode pagos = factura.path("infoFactura").path("pagos").path("pago");
        if (pagos.isArray()) {
            sincronizarPagos(comprobante, pagos, usuarioAuditoria, ahora);
        }

        if (persistirInformacionAdicional) {
            persistirInformacionAdicional(comprobante,
                    factura.path("infoAdicional").path("campoAdicional"), usuarioAuditoria, ahora);
        }
        if (persistirRetenciones) {
            persistirRetenciones(comprobante, factura.path("impuestos").path("impuesto"), usuarioAuditoria, ahora);
        }
    }

    private ComprobanteDetalle buscarDetalle(Long comprobanteId, Integer numeroLinea) {
        return entityManager.createQuery(
                "select d from ComprobanteDetalle d "
                        + "where d.comprobante.id = :comprobanteId and d.numeroLinea = :numeroLinea",
                ComprobanteDetalle.class)
                .setParameter("comprobanteId", comprobanteId)
                .setParameter("numeroLinea", numeroLinea)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private void sincronizarImpuestos(ComprobanteDetalle detalle, JsonNode nodos, String usuario, LocalDateTime ahora) {
        List<ComprobanteDetalleImpuesto> existentes = entityManager.createQuery(
                "select i from ComprobanteDetalleImpuesto i "
                        + "where i.comprobanteDetalle.id = :detalleId order by i.id",
                ComprobanteDetalleImpuesto.class)
                .setParameter("detalleId", detalle.getId())
                .getResultList();
        Set<ComprobanteDetalleImpuesto> procesados = new HashSet<>();

        for (JsonNode nodo : nodos) {
            String codigo = texto(nodo, "codigo");
            String porcentaje = texto(nodo, "codigoPorcentaje");
            ComprobanteDetalleImpuesto impuesto = existentes.stream()
                    .filter(actual -> !procesados.contains(actual))
                    .filter(actual -> java.util.Objects.equals(actual.getCodigoImpuesto(), codigo))
                    .filter(actual -> java.util.Objects.equals(actual.getCodigoPorcentaje(), porcentaje))
                    .findFirst()
                    .orElse(null);
            boolean nuevo = impuesto == null;
            if (nuevo) {
                impuesto = new ComprobanteDetalleImpuesto();
                impuesto.setComprobanteDetalle(detalle);
            }

            impuesto.setCodigoImpuesto(codigo);
            impuesto.setCodigoPorcentaje(porcentaje);
            impuesto.setTarifa(decimal(nodo, "tarifa"));
            impuesto.setBaseImponible(decimal(nodo, "baseImponible"));
            impuesto.setValor(decimal(nodo, "valor"));
            impuesto.setValorDevolucionIva(decimal(nodo, "valorDevolucionIva"));
            impuesto.setEstadoRegistro(EstadoRegistro.ACTIVO);
            if (nuevo) {
                auditar(impuesto, usuario, ahora);
                entityManager.persist(impuesto);
            } else {
                auditarModificacion(impuesto, usuario, ahora);
                procesados.add(impuesto);
            }
        }

        for (ComprobanteDetalleImpuesto impuesto : existentes) {
            if (!procesados.contains(impuesto)) {
                impuesto.setEstadoRegistro(EstadoRegistro.ELIMINADO);
                auditarModificacion(impuesto, usuario, ahora);
            }
        }
    }

    private void sincronizarPagos(Comprobante comprobante, JsonNode nodos, String usuario, LocalDateTime ahora) {
        List<ComprobantePago> existentes = entityManager.createQuery(
                "select p from ComprobantePago p where p.comprobante.id = :comprobanteId order by p.id",
                ComprobantePago.class)
                .setParameter("comprobanteId", comprobante.getId())
                .getResultList();

        int indice = 0;
        for (JsonNode nodo : nodos) {
            ComprobantePago pago = indice < existentes.size() ? existentes.get(indice) : null;
            boolean nuevo = pago == null;
            if (nuevo) {
                pago = new ComprobantePago();
                pago.setComprobante(comprobante);
            }

            pago.setCodigoFormaPago(texto(nodo, "formaPago"));
            pago.setTotal(decimal(nodo, "total"));
            pago.setPlazo(entero(nodo, "plazo"));
            pago.setUnidadTiempo(texto(nodo, "unidadTiempo"));
            pago.setEstadoRegistro(EstadoRegistro.ACTIVO);
            log.debug("ID_TRANSACCION={} - Pago {} antes de persistir: formaPago={}, total={}, plazo={}, unidadTiempo={}",
                    comprobante.getIdTransaccion(), indice + 1, pago.getCodigoFormaPago(), pago.getTotal(),
                    pago.getPlazo(), pago.getUnidadTiempo());
            if (nuevo) {
                auditar(pago, usuario, ahora);
                entityManager.persist(pago);
            } else {
                auditarModificacion(pago, usuario, ahora);
            }
            indice++;
        }

        for (int i = indice; i < existentes.size(); i++) {
            ComprobantePago pago = existentes.get(i);
            pago.setEstadoRegistro(EstadoRegistro.ELIMINADO);
            auditarModificacion(pago, usuario, ahora);
        }
    }

    /**
     * Comprueba la existencia de registros de una sección para el comprobante.
     * El nombre de tabla procede exclusivamente de constantes internas.
     */
    private boolean existenRegistros(String tabla, Long comprobanteId) {
        Number cantidad = (Number) entityManager.createNativeQuery(
                "select count(*) from " + tabla + " where comprobante_id = :id")
                .setParameter("id", comprobanteId)
                .getSingleResult();
        return cantidad.longValue() > 0;
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

    private void auditarModificacion(EstadoRegistroEntity entidad, String usuario, LocalDateTime ahora) {
        if (entidad instanceof ComprobanteDetalle e) {
            e.setUsuarioModificacion(usuario);
            e.setFechaModificacion(ahora);
        } else if (entidad instanceof ComprobanteDetalleImpuesto e) {
            e.setUsuarioModificacion(usuario);
            e.setFechaModificacion(ahora);
        } else if (entidad instanceof ComprobantePago e) {
            e.setUsuarioModificacion(usuario);
            e.setFechaModificacion(ahora);
        } else if (entidad instanceof ComprobanteInformacionAdicional e) {
            e.setUsuarioModificacion(usuario);
            e.setFechaModificacion(ahora);
        } else if (entidad instanceof ComprobanteRetencion e) {
            e.setUsuarioModificacion(usuario);
            e.setFechaModificacion(ahora);
        }
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() ? null : valor.asText();
    }

    private BigDecimal decimal(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        if (valor == null || valor.isNull() || valor.isContainerNode()) {
            return null;
        }

        String texto = valor.asText();
        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException exception) {
            log.debug("No se pudo convertir a decimal el campo '{}': '{}'", campo, texto);
            return null;
        }
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
