package ec.dalara.factucore.application.workflow;

import ec.dalara.factucore.application.workflow.WorkflowEtapaExecutor;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ec.dalara.factucore.application.ApplicationException;

import ec.dalara.factucore.application.port.out.ComprobanteEvidenciaPort;
import ec.dalara.factucore.application.port.out.DocumentoDefinitionProvider;
import ec.dalara.factucore.application.port.out.XmlValidatorPort;
import ec.dalara.factucore.application.service.ComprobanteService;
import ec.dalara.factucore.application.service.DocumentoXsdService;
import ec.dalara.factucore.application.service.EmisionService;
import ec.dalara.factucore.application.service.EmpresaService;
import ec.dalara.factucore.application.service.VersionDocumentoXsdService;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.application.validation.ComprobanteValidationResult;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidacionXsdWorkflowStep {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;	private final XmlValidatorPort xmlValidator;
	private final ComprobanteEvidenciaPort evidenciaPort;
	private final ComprobanteService comprobanteService;
	private final EmpresaService empresaService;
	private final EmisionService emisionService;
	private final DocumentoDefinitionProvider definitionProvider;
	private final DocumentoXsdService documentoXsdService;
	private final VersionDocumentoXsdService versionDocumentoXsdService;
	private final PersistenciaSeccionesComprobanteService persistenciaSeccionesComprobanteService;
	private final SriProperties sriProperties;

	public EtapaWorkflow etapa() {
		return EtapaWorkflow.VALIDACION_XSD;
	}

	/**
	 * Valida el XML y, únicamente si la validación es exitosa, persiste el
	 * comprobante. La persistencia forma parte de esta etapa y no constituye un
	 * nodo independiente del workflow.
	 */
	@Transactional
	public String ejecutar(ContextoWorkflow contexto) {
		return workflowEtapaExecutor.ejecutar(contexto, etapa(), () -> ejecutarResultado(contexto));
	}

	private ResultadoEtapa ejecutarResultado(ContextoWorkflow contexto) {
		if (contexto == null || contexto.getXml() == null || contexto.getXml().isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_XML_REQUERIDO);
		}

		var definition = contexto.getDefinicionDocumento();
		if (definition == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA);
		}

		// Si el XML no cumple el XSD, se lanza la excepción y no se persiste.
		ComprobanteValidationResult validacion = xmlValidator.validar(contexto.getXml(), definition);
		contexto.setValidacionXsd(validacion);
		if (!validacion.esValido()) {
			String codigo = validacion.getErrores().isEmpty() ? MessageCodes.WORKFLOW_ETAPA_ERROR : validacion.getErrores().get(0).getCodigo();
			String mensaje = validacion.getErrores().isEmpty() ? null : validacion.getErrores().get(0).getMensaje();
			return ResultadoEtapa.fallida(etapa(), null, codigo, mensaje,
					java.util.Map.of("validacion", validacion));
		}

		// La persistencia ocurre solamente después de validar correctamente el XML.
		boolean comprobanteNuevo = persistirComprobante(contexto, definition);
		if (comprobanteNuevo) {
			persistenciaSeccionesComprobanteService.persistir(contexto.getComprobante(),
					contexto.getSolicitud().getDatos(), contexto.getSolicitud().getUsuario());
		}

		evidenciaPort.guardarXmlGenerado(contexto.getComprobanteId(), contexto.getXml(),
				contexto.getSolicitud().getUsuario());

		contexto.getComprobante().setEstadoProceso(EstadoProceso.XSD_VALIDADO.name());

		return ResultadoEtapa.exitosa(etapa(), EstadoProceso.XSD_VALIDADO.name());
	}

	private boolean persistirComprobante(ContextoWorkflow contexto,
			ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel definition) {
		var solicitud = contexto.getSolicitud();

		if (solicitud == null) {
			throw new ApplicationException(MessageCodes.COMPROBANTE_REQUEST_REQUERIDO);
		}

		if (contexto.getClaveAcceso() == null || contexto.getClaveAcceso().isBlank()) {
			throw new ApplicationException(MessageCodes.CLAVE_ACCESO_REQUERIDA);
		}

		// En una reanudación el comprobante ya existe; no se crea otro registro.
		if (contexto.getComprobante() != null) {
			return false;
		}

		var empresa = empresaService.obtenerPorId(solicitud.getIdEmpresa())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_EMPRESA_REQUERIDA));

		var emision = emisionService.resolver(solicitud.getIdEmpresa(), solicitud.getIdEstablecimiento(),
				solicitud.getIdPuntoEmision());

		var documentoXsd = documentoXsdService.obtenerPorCodigo(definition.getDocumento().getCodigo())
				.orElseThrow(() -> new ApplicationException(MessageCodes.COMPROBANTE_DEFINICION_NO_ENCONTRADA));

		var versionXsd = versionDocumentoXsdService.obtenerPorId(definition.getVersion().getId())
				.orElseThrow(() -> new ApplicationException(MessageCodes.XSD_VERSION_NO_ENCONTRADA));

		if (contexto.getComprobanteReemplazado() == null) {
			var existente = comprobanteService.obtenerPorClaveAcceso(contexto.getClaveAcceso())
					.filter(c -> !EstadoRegistro.ELIMINADO.equals(c.getEstadoRegistro()));

			if (existente.isPresent()) {
				contexto.asignarComprobante(existente.get());
				return false;
			}
		}

		Comprobante comprobante = Comprobante.builder()
				.empresa(empresa)
				.establecimiento(emision.establecimiento())
				.puntoEmision(emision.puntoEmision())
				.documentoXsd(documentoXsd)
				.versionDocumentoXsd(versionXsd)
				.idTransaccion(solicitud.getIdTransaccion())
				.idDocumentoOrigen(solicitud.getIdDocumentoOrigen())
				.hashIdentidad(contexto.getHashIdentidad())
				.hashContenido(contexto.getHashContenido())
				.comprobanteReemplazado(contexto.getComprobanteReemplazado())
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
				.estadoRegistro(EstadoRegistro.ACTIVO)
				.usuarioCreacion(solicitud.getUsuario())
				.fechaCreacion(LocalDateTime.now())
				.build();

		calcularDatosPersistidos(comprobante, solicitud.getDatos());
		comprobante = comprobanteService.guardar(comprobante);
		if (contexto.getComprobanteReemplazado() != null) {
			var anterior = contexto.getComprobanteReemplazado();
			anterior.setEstadoRegistro(EstadoRegistro.REEMPLAZADO);
			anterior.setUsuarioModificacion(solicitud.getUsuario());
			anterior.setFechaModificacion(LocalDateTime.now());
			comprobanteService.guardar(anterior);
		}
		contexto.asignarComprobante(comprobante);
		return true;
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
