package ec.dalara.factucore.application.workflow;

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
import ec.dalara.factucore.application.validation.ComprobanteValidationResult;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EstadoProceso;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;
import ec.dalara.factucore.domain.workflow.ResultadoEtapa;
import ec.dalara.factucore.infrastructure.configuration.sri.SriProperties;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidacionXsdWorkflowStep {

	private final WorkflowEtapaExecutor workflowEtapaExecutor;
	private final XmlValidatorPort xmlValidator;
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
			String codigo = validacion.getErrores().isEmpty() ? MessageCodes.WORKFLOW_ETAPA_ERROR
					: validacion.getErrores().get(0).getCodigo();
			String mensaje = validacion.getErrores().isEmpty() ? null : validacion.getErrores().get(0).getMensaje();
			return ResultadoEtapa.fallida(etapa(), null, codigo, mensaje, java.util.Map.of("validacion", validacion));
		}

		// La persistencia ocurre solamente después de validar correctamente el XML.
		boolean comprobanteNuevo = persistirComprobante(contexto, definition);
		if (!comprobanteNuevo) {
			log.debug("ID_TRANSACCION={} - Reanudando persistencia de secciones del comprobante {}",
					contexto.getSolicitud().getIdTransaccion(), contexto.getComprobanteId());
		}
		persistenciaSeccionesComprobanteService.persistir(contexto.getComprobante(), contexto.getSolicitud().getDatos(),
				contexto.getSolicitud().getUsuario());

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
			Comprobante existente = contexto.getComprobante();
			if (existente.getNumComprobante() == null || existente.getNumComprobante().isBlank()) {
				String numero = resolverNumeroComprobante(contexto, existente.getEstablecimiento().getCodigo(),
						existente.getPuntoEmision().getCodigo());
				if (numero != null) {
					existente.setNumComprobante(numero);
					existente.setUsuarioModificacion(solicitud.getUsuario());
					existente.setFechaModificacion(LocalDateTime.now());
					comprobanteService.guardar(existente);
				}
			}
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
				Comprobante comprobanteExistente = existente.get();
				comprobanteExistente.setNumComprobante(resolverNumeroComprobante(contexto,
						emision.establecimiento().getCodigo(), emision.puntoEmision().getCodigo()));
				calcularDatosPersistidos(comprobanteExistente, solicitud.getDatos());
				comprobanteExistente.setUsuarioModificacion(solicitud.getUsuario());
				comprobanteExistente.setFechaModificacion(LocalDateTime.now());
				comprobanteExistente = comprobanteService.guardar(comprobanteExistente);
				contexto.asignarComprobante(comprobanteExistente);
				log.debug("ID_TRANSACCION={} - Cabecera del comprobante {} sincronizada desde la solicitud",
						solicitud.getIdTransaccion(), comprobanteExistente.getId());
				return false;
			}
		}

		Comprobante comprobante = Comprobante.builder().empresa(empresa).establecimiento(emision.establecimiento())
				.puntoEmision(emision.puntoEmision()).documentoXsd(documentoXsd).versionDocumentoXsd(versionXsd)
				.idTransaccion(solicitud.getIdTransaccion()).idDocumentoOrigen(solicitud.getIdDocumentoOrigen())
				.hashIdentidad(contexto.getHashIdentidad()).hashContenido(contexto.getHashContenido())
				.comprobanteReemplazado(contexto.getComprobanteReemplazado())
				.ambiente(resolverAmbiente(sriProperties.getAmbiente())).tipoEmision("1")
				.codigoDocumento(definition.getDocumento().getCodigo()).secuencial(contexto.getSecuencial())
				.numComprobante(resolverNumeroComprobante(contexto, emision.establecimiento().getCodigo(),
						emision.puntoEmision().getCodigo()))
				.claveAcceso(contexto.getClaveAcceso()).fechaEmision(solicitud.getFechaInicio().toLocalDate())
				.razonSocialEmisor(empresa.getRazonSocial()).nombreComercialEmisor(empresa.getNombreComercial())
				.rucEmisor(empresa.getRuc()).direccionMatrizEmisor(empresa.getDireccionMatriz())
				.direccionEstablecimientoEmisor(emision.establecimiento().getDireccion())
				.estadoProceso(EstadoProceso.CLAVE_ACCESO_GENERADA.name()).estadoRegistro(EstadoRegistro.ACTIVO)
				.usuarioCreacion(solicitud.getUsuario()).fechaCreacion(LocalDateTime.now()).build();

		calcularDatosPersistidos(comprobante, solicitud.getDatos());
		log.debug(
				"ID_TRANSACCION={} - Importes de cabecera antes de persistir: subtotal={}, descuento={}, sinImpuestos={}, impuestos={}, propina={}, total={}",
				solicitud.getIdTransaccion(), comprobante.getSubtotal(), comprobante.getTotalDescuento(),
				comprobante.getTotalSinImpuestos(), comprobante.getTotalImpuestos(), comprobante.getTotalPropina(),
				comprobante.getTotalAPagar());
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

	private String resolverNumeroComprobante(ContextoWorkflow contexto, String codigoEstablecimiento,
			String codigoPuntoEmision) {
		String numero = contexto.getNumComprobante();
		if (numero == null || numero.isBlank()) {
			Object generado = contexto.getValoresGenerados().get("numComprobante");
			numero = generado == null ? null : generado.toString();
		}
		if (numero == null || numero.isBlank()) {
			String secuencial = contexto.getSecuencial();
			if (secuencial == null || secuencial.isBlank()) {
				return null;
			}
			numero = String.format(java.util.Locale.ROOT, "%03d-%03d-%09d", Integer.parseInt(codigoEstablecimiento),
					Integer.parseInt(codigoPuntoEmision), Long.parseLong(secuencial));
			contexto.setNumComprobante(numero);
		}
		return numero;
	}

	private void calcularDatosPersistidos(Comprobante comprobante, JsonNode datos) {
		if (datos == null || !datos.isObject())
			return;
		JsonNode datosDocumento = datos.has("datos") && datos.path("datos").isObject() ? datos.path("datos") : datos;
		JsonNode factura = datosDocumento.path("comprobante").path("factura");
		if (!factura.isObject()) {
			factura = datosDocumento.path("factura");
		}
		if (!factura.isObject())
			return;
		JsonNode infoFactura = factura.path("infoFactura");
		String razonSocial = texto(infoFactura, "razonSocialComprador");
		String identificacion = texto(infoFactura, "identificacionComprador");
		String direccion = texto(infoFactura, "direccionComprador");
		if (razonSocial != null)
			comprobante.setRazonSocialReceptor(razonSocial);
		if (identificacion != null)
			comprobante.setIdentificacionReceptor(identificacion);
		if (direccion != null)
			comprobante.setDireccionReceptor(direccion);
		comprobante.setCorreoComprador(resolverCorreo(datos, factura));

		JsonNode detalles = factura.path("detalles").path("detalle");
		if (!detalles.isArray())
			return;
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
			if (cantidad != null && precio != null)
				subtotal = subtotal.add(cantidad.multiply(precio));
			else if (totalLinea != null)
				subtotal = subtotal.add(totalLinea).add(desc);
			descuento = descuento.add(desc);
			if (totalLinea != null) {
				sinImpuestos = sinImpuestos.add(totalLinea);
				hayTotalLinea = true;
			}
			JsonNode lineTaxes = detalle.path("impuestos").path("impuesto");
			if (lineTaxes.isArray())
				for (JsonNode tax : lineTaxes)
					impuestos = impuestos.add(cero(decimal(tax, "valor")));
		}
		if (!hayTotalLinea)
			sinImpuestos = subtotal.subtract(descuento);
		JsonNode totalTaxes = infoFactura.path("totalConImpuestos").path("totalImpuesto");
		if (totalTaxes.isArray()) {
			impuestos = BigDecimal.ZERO;
			for (JsonNode tax : totalTaxes)
				impuestos = impuestos.add(cero(decimal(tax, "valor")));
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
				? datos.path("datos")
				: datos;
		String externo = texto(datosDocumento, "correo");
		if (externo != null && !externo.isBlank())
			return externo.trim();
		JsonNode campos = factura.path("infoAdicional").path("campoAdicional");
		if (campos.isArray())
			for (JsonNode campo : campos) {
				String nombre = texto(campo, "nombre");
				String valor = texto(campo, "valor");
				if ("Correo".equalsIgnoreCase(nombre) && valor != null && !valor.isBlank())
					return valor.trim();
			}
		return null;
	}

	private String texto(JsonNode nodo, String campo) {
		JsonNode valor = nodo == null ? null : nodo.get(campo);
		return valor == null || valor.isNull() || valor.isContainerNode() ? null : valor.asText();
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

	private BigDecimal cero(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private BigDecimal redondear(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP);
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
