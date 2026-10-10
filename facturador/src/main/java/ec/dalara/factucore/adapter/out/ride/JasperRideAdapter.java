package ec.dalara.factucore.adapter.out.ride;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.application.service.ComprobanteDetalleImpuestoService;
import ec.dalara.factucore.application.service.ComprobanteDetalleService;
import ec.dalara.factucore.application.service.ComprobanteInformacionAdicionalService;
import ec.dalara.factucore.application.service.ComprobantePagoService;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalle;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteDetalleImpuesto;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobanteInformacionAdicional;
import ec.dalara.factucore.infrastructure.persistence.entity.ComprobantePago;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

@Component
@RequiredArgsConstructor
public class JasperRideAdapter implements RidePort {

	private static final String FACTURA = "factura";
	private static final String NOTA_CREDITO = "notaCredito";
	private static final String NOTA_DEBITO = "notaDebito";
	private static final String GUIA_REMISION = "guiaRemision";
	private static final String COMPROBANTE_RETENCION = "comprobanteRetencion";

	private final ComprobanteDetalleService comprobanteDetalleService;
	private final ComprobanteDetalleImpuestoService comprobanteDetalleImpuestoService;
	private final ComprobantePagoService comprobantePagoService;
	private final ComprobanteInformacionAdicionalService comprobanteInformacionAdicionalService;
	private final MessageResolver messageResolver;

	@Value("${factucore.path.jasper}")
	private String rutaJasper;

	@Override
	@Transactional(readOnly = true)
	public byte[] generar(Comprobante comprobante, String nombreComprobante) {
		if (comprobante == null || comprobante.getId() == null) {
			throw new ApplicationException(MessageCodes.RIDE_COMPROBANTE_REQUERIDO);
		}

		if (nombreComprobante == null || nombreComprobante.isBlank()) {
			throw new ApplicationException(MessageCodes.RIDE_NOMBRE_COMPROBANTE_REQUERIDO);
		}

		String nombrePlantilla = normalizarNombre(nombreComprobante);
		Path directorio = Path.of(rutaJasper);
		Path plantilla = directorio.resolve(nombrePlantilla + ".jrxml");

		if (!Files.isDirectory(directorio) || !Files.isRegularFile(plantilla)) {
			throw new ApplicationException(MessageCodes.RIDE_PLANTILLA_NO_ENCONTRADA, nombrePlantilla);
		}

		Path directorioCompilado = null;

		try {
			Map<String, Object> parametros = construirParametros(comprobante);
			directorioCompilado = Files.createTempDirectory("factucore-jasper-");
			compilarSubreportes(nombrePlantilla, directorio, directorioCompilado, parametros);

			JasperReport reporte = JasperCompileManager.compileReport(plantilla.toString());
			JRDataSource dataSource = crearDataSource(nombrePlantilla, comprobante, parametros);
			JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, parametros, dataSource);

			return JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (JRException exception) {
			throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception);
		} catch (IOException exception) {
			throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception);
		} finally {
			eliminarDirectorioTemporal(directorioCompilado);
		}
	}

	private Map<String, Object> construirParametros(Comprobante comprobante) {
		Map<String, Object> parametros = new LinkedHashMap<>();

		parametros.put("RUC", comprobante.getRucEmisor());
		parametros.put("NUM_AUT", comprobante.getNumeroAutorizacion());
		parametros.put("FECHA_AUT", valor(comprobante.getFechaAutorizacion()));
		parametros.put("TIPO_EMISION", comprobante.getTipoEmision());
		parametros.put("CLAVE_ACC", comprobante.getClaveAcceso());
		parametros.put("RAZON_SOCIAL", comprobante.getRazonSocialEmisor());
		parametros.put("DIR_MATRIZ", comprobante.getDireccionMatrizEmisor());
		parametros.put("DIR_SUCURSAL", comprobante.getDireccionEstablecimientoEmisor());
		parametros.put("RS_COMPRADOR", comprobante.getRazonSocialReceptor());
		parametros.put("RUC_COMPRADOR", comprobante.getIdentificacionReceptor());
		parametros.put("DIRECCION_CLIENTE", comprobante.getDireccionReceptor());
		parametros.put("FECHA_EMISION", valor(comprobante.getFechaEmision()));
		parametros.put("NUM_FACT", comprobante.getSecuencial());
		parametros.put("AMBIENTE", comprobante.getAmbiente());
		parametros.put("NOM_COMERCIAL", comprobante.getNombreComercialEmisor());

		parametros.put("GUIA", null);
		parametros.put("CONT_ESPECIAL", null);
		parametros.put("LLEVA_CONTABILIDAD", null);
		parametros.put("NEGOCIABLE", null);
		parametros.put("REGIMEN_TRIBUTARIO", null);
		parametros.put("AGENTE_RETENCION", null);

		List<ComprobanteDetalle> detalles = comprobanteDetalleService.listarPorComprobante(comprobante.getId());
		BigDecimal descuento = detalles.stream().map(ComprobanteDetalle::getDescuento).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalSinImpuestos = detalles.stream().map(ComprobanteDetalle::getPrecioTotalSinImpuesto)
				.filter(this::noNulo).reduce(BigDecimal.ZERO, BigDecimal::add);

		List<ComprobanteDetalleImpuesto> impuestos = obtenerImpuestos(detalles);
		BigDecimal iva = impuestos.stream().filter(impuesto -> "2".equals(impuesto.getCodigoImpuesto()))
				.map(ComprobanteDetalleImpuesto::getValor).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal ice = impuestos.stream().filter(impuesto -> "3".equals(impuesto.getCodigoImpuesto()))
				.map(ComprobanteDetalleImpuesto::getValor).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal total = totalSinImpuestos.add(impuestos.stream().map(ComprobanteDetalleImpuesto::getValor)
				.filter(this::noNulo).reduce(BigDecimal.ZERO, BigDecimal::add));

		parametros.put("DESCUENTO", descuento);
		parametros.put("TOTAL_SIN_IMP", totalSinImpuestos);
		parametros.put("TOTAL_SIN_SUBSIDIO", totalSinImpuestos);
		parametros.put("IVA", iva);
		parametros.put("ICE", ice);
		parametros.put("TOTAL", total);
		parametros.put("IVA_12", iva);
		parametros.put("IVA_0", BigDecimal.ZERO);
		parametros.put("NO_OBJETO_IVA", BigDecimal.ZERO);
		parametros.put("EXENTO_IVA", BigDecimal.ZERO);
		parametros.put("PORCENTAJE_IVA", null);
		parametros.put("AHORRO_POR_SUBSIDIO", BigDecimal.ZERO);

		parametros.put("RAZON_MODIF", null);
		parametros.put("DOC_MODIFICADO", null);
		parametros.put("NUM_DOC_MODIFICADO", null);
		parametros.put("FECHA_EMISION_DOC_SUSTENTO", null);
		parametros.put("EJERCICIO_FISCAL", null);
		parametros.put("FECHA_INI_TRANSPORTE", null);
		parametros.put("FECHA_FIN_TRANSPORTE", null);
		parametros.put("RUC_TRANSPORTISTA", null);
		parametros.put("RS_TRANSPORTISTA", null);
		parametros.put("PLACA", null);
		parametros.put("PUNTO_PARTIDA", null);

		parametros.put("INFO_ADICIONAL", construirInformacionAdicional(comprobante.getId()));

		return parametros;
	}

	private JRDataSource crearDataSource(String nombrePlantilla, Comprobante comprobante,
			Map<String, Object> parametros) {
		List<Map<String, Object>> filas = new ArrayList<>();

		if (FACTURA.equals(nombrePlantilla) || NOTA_CREDITO.equals(nombrePlantilla)
				|| GUIA_REMISION.equals(nombrePlantilla)) {
			filas = construirFilasDetalle(comprobante.getId());
		}

		if (filas.isEmpty()) {
			filas.add(new LinkedHashMap<>());
		}

		Collection<Map<String, Object>> infoAdicional = construirInformacionAdicional(comprobante.getId());
		Collection<Map<String, Object>> formasPago = construirFormasPago(comprobante.getId());
		Collection<Map<String, Object>> totales = construirTotales(comprobante.getId());

		for (Map<String, Object> fila : filas) {
			fila.put("infoAdicional", infoAdicional);
			fila.put("formasPago", formasPago);
			fila.put("totalesComprobante", totales);
		}

		parametros.put("INFO_ADICIONAL", infoAdicional);
		return new JRMapCollectionDataSource(new ArrayList<Map<String, ?>>(filas));
	}

	private List<Map<String, Object>> construirFilasDetalle(Long comprobanteId) {
		List<Map<String, Object>> filas = new ArrayList<>();

		for (ComprobanteDetalle detalle : comprobanteDetalleService.listarPorComprobante(comprobanteId)) {
			Map<String, Object> fila = new LinkedHashMap<>();
			fila.put("codigoPrincipal", detalle.getCodigoPrincipal());
			fila.put("codigoAuxiliar", detalle.getCodigoAuxiliar());
			fila.put("cantidad", detalle.getCantidad());
			fila.put("descripcion", detalle.getDescripcion());
			fila.put("precioUnitario", detalle.getPrecioUnitario());
			fila.put("precioSinSubsidio", null);
			fila.put("precioTotalSinImpuesto", detalle.getPrecioTotalSinImpuesto());
			fila.put("detalle1", null);
			fila.put("detalle2", null);
			fila.put("detalle3", null);
			fila.put("descuento", detalle.getDescuento());
			filas.add(fila);
		}

		return filas;
	}

	private Collection<Map<String, Object>> construirFormasPago(Long comprobanteId) {
		List<Map<String, Object>> resultado = new ArrayList<>();

		for (ComprobantePago pago : comprobantePagoService.listarPorComprobante(comprobanteId)) {
			Map<String, Object> fila = new LinkedHashMap<>();
			fila.put("formaPago", pago.getCodigoFormaPago());
			fila.put("valor", valor(pago.getTotal()));
			resultado.add(fila);
		}

		return resultado;
	}

	private Collection<Map<String, Object>> construirInformacionAdicional(Long comprobanteId) {
		List<Map<String, Object>> resultado = new ArrayList<>();

		for (ComprobanteInformacionAdicional informacion : comprobanteInformacionAdicionalService
				.listarPorComprobante(comprobanteId)) {
			Map<String, Object> fila = new LinkedHashMap<>();
			fila.put("nombre", informacion.getNombre());
			fila.put("valor", informacion.getValor());
			resultado.add(fila);
		}

		return resultado;
	}

	private Collection<Map<String, Object>> construirTotales(Long comprobanteId) {
		List<ComprobanteDetalle> detalles = comprobanteDetalleService.listarPorComprobante(comprobanteId);
		List<ComprobanteDetalleImpuesto> impuestos = obtenerImpuestos(detalles);

		BigDecimal subtotal = detalles.stream().map(ComprobanteDetalle::getPrecioTotalSinImpuesto).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal descuento = detalles.stream().map(ComprobanteDetalle::getDescuento).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalImpuestos = impuestos.stream().map(ComprobanteDetalleImpuesto::getValor).filter(this::noNulo)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<Map<String, Object>> resultado = new ArrayList<>();
		resultado.add(total(messageResolver.resolver(MessageCodes.RIDE_TOTAL_SUBTOTAL_SIN_IMPUESTOS), subtotal, false));
		if (descuento.signum() != 0) {
			resultado.add(total(messageResolver.resolver(MessageCodes.RIDE_TOTAL_DESCUENTO), descuento, true));
		}
		for (ComprobanteDetalleImpuesto impuesto : impuestos) {
			String descripcion = messageResolver.resolver(MessageCodes.RIDE_TOTAL_IMPUESTO,
					impuesto.getCodigoImpuesto(), impuesto.getCodigoPorcentaje());
			resultado.add(total(descripcion, impuesto.getValor(), false));
		}
		resultado.add(total(messageResolver.resolver(MessageCodes.RIDE_TOTAL_TOTAL), subtotal.add(totalImpuestos), false));

		return resultado;
	}

	private Map<String, Object> total(String descripcion, BigDecimal valor, boolean esNegativo) {
		Map<String, Object> fila = new LinkedHashMap<>();
		fila.put("descripcion", descripcion);
		fila.put("valor", valor);
		fila.put("esNegativo", esNegativo);
		return fila;
	}

	private List<ComprobanteDetalleImpuesto> obtenerImpuestos(List<ComprobanteDetalle> detalles) {
		List<ComprobanteDetalleImpuesto> impuestos = new ArrayList<>();
		for (ComprobanteDetalle detalle : detalles) {
			impuestos.addAll(comprobanteDetalleImpuestoService.listarPorComprobanteDetalle(detalle.getId()));
		}
		return impuestos;
	}

	private boolean noNulo(BigDecimal valor) {
		return valor != null;
	}

	private void compilarSubreportes(String nombrePlantilla, Path origen, Path destino, Map<String, Object> parametros)
			throws JRException {
		List<String> subreportes = switch (nombrePlantilla) {
		case FACTURA -> List.of("facturaInfoAdicional.jrxml", "facturaFormasPago.jrxml", "totalesComprobante.jrxml");
		case NOTA_CREDITO -> List.of("facturaInfoAdicional.jrxml", "totalesComprobante.jrxml");
		case NOTA_DEBITO ->
			List.of("facturaInfoAdicional.jrxml", "facturaFormasPago.jrxml", "totalesComprobante.jrxml");
		case GUIA_REMISION -> List.of("guiaRemisionDetalles.jrxml", "facturaInfoAdicional.jrxml");
		case COMPROBANTE_RETENCION -> List.of("facturaInfoAdicional.jrxml");
		default -> Collections.emptyList();
		};

		for (String subreporte : subreportes) {
			Path archivo = origen.resolve(subreporte);
			Path compilado = destino.resolve(subreporte.replace(".jrxml", ".jasper"));
			JasperCompileManager.compileReportToFile(archivo.toString(), compilado.toString());
		}

		String directorio = destino.toAbsolutePath().toString() + java.io.File.separator;
		parametros.put("SUBREPORT_DIR", directorio);
		parametros.put("SUBREPORT_INFO_ADICIONAL", directorio);
		parametros.put("SUBREPORT_PAGOS", directorio);
		parametros.put("SUBREPORT_TOTALES", directorio);
	}

	private String normalizarNombre(String nombre) {
		String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		String limpio = sinAcentos.replaceAll("[^A-Za-z0-9]+", " ").trim().toLowerCase(Locale.ROOT);
		StringBuilder resultado = new StringBuilder();

		for (String parte : limpio.split(" ")) {
			if (parte.isBlank()) {
				continue;
			}
			if (resultado.isEmpty()) {
				resultado.append(parte);
			} else {
				resultado.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
			}
		}

		return resultado.toString();
	}

	private String valor(Object valor) {
		return valor == null ? null : String.valueOf(valor);
	}

	private void eliminarDirectorioTemporal(Path directorio) {
		if (directorio == null || !Files.exists(directorio)) {
			return;
		}

		try (var archivos = Files.walk(directorio)) {
			archivos.sorted((a, b) -> b.compareTo(a)).forEach(archivo -> {
				try {
					Files.deleteIfExists(archivo);
				} catch (IOException ignored) {
				}
			});
		} catch (IOException ignored) {
		}
	}
}
