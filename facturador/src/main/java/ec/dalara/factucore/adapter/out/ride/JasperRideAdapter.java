package ec.dalara.factucore.adapter.out.ride;

import java.io.IOException;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.RidePort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Comprobante;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Component
@RequiredArgsConstructor
public class JasperRideAdapter implements RidePort {

	private static final String FACTURA = "factura";
	private static final String NOTA_CREDITO = "notaCredito";
	private static final String NOTA_DEBITO = "notaDebito";
	private static final String GUIA_REMISION = "guiaRemision";
	private static final String COMPROBANTE_RETENCION = "comprobanteRetencion";

	private final ObjectMapper objectMapper;

	@Value("${factucore.path.jasper}")
	private String rutaJasper;

	@Override
	public byte[] generar(Comprobante comprobante, String nombreComprobante) {
		if (comprobante == null || comprobante.getDatosComprobante() == null) {
			throw new ApplicationException(MessageCodes.RIDE_COMPROBANTE_REQUERIDO);
		}

		if (nombreComprobante == null || nombreComprobante.isBlank()) {
			throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, "Nombre del comprobante requerido");
		}

		String nombrePlantilla = normalizarNombre(nombreComprobante);
		Path directorio = Path.of(rutaJasper);
		Path plantilla = directorio.resolve(nombrePlantilla + ".jrxml");

		if (!Files.isDirectory(directorio) || !Files.isRegularFile(plantilla)) {
			throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR,
					"No existe la plantilla RIDE para el comprobante: " + plantilla);
		}

		Path directorioCompilado = null;

		try {
			JsonNode datos = objectMapper.readTree(comprobante.getDatosComprobante());
			Map<String, Object> parametros = construirParametros(comprobante, datos);

			directorioCompilado = Files.createTempDirectory("factucore-jasper-");
			compilarSubreportes(nombrePlantilla, directorio, directorioCompilado, parametros);

			JasperReport reporte = JasperCompileManager.compileReport(plantilla.toString());
			JRDataSource dataSource = crearDataSource(nombrePlantilla, datos, parametros);
			JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, parametros, dataSource);

			return JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (IOException | JRException exception) {
			throw new ApplicationException(MessageCodes.RIDE_GENERACION_ERROR, exception.getMessage());
		} finally {
			eliminarDirectorioTemporal(directorioCompilado);
		}
	}

	private Map<String, Object> construirParametros(Comprobante comprobante, JsonNode datos) {
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

		parametros.put("GUIA", buscarValor(datos, "guiaRemision"));
		parametros.put("CONT_ESPECIAL", buscarValor(datos, "contribuyenteEspecial"));
		parametros.put("LLEVA_CONTABILIDAD", buscarValor(datos, "obligadoContabilidad"));
		parametros.put("NEGOCIABLE", buscarBoolean(datos, "tipoNegociable"));
		parametros.put("REGIMEN_TRIBUTARIO", buscarValor(datos, "regimenRimpe"));
		parametros.put("REGIMEN_RIMPE", buscarValor(datos, "regimenRimpe"));
		parametros.put("AGENTE_RETENCION", buscarValor(datos, "agenteRetencion"));
		parametros.put("DESCUENTO", buscarValor(datos, "totalDescuento"));
		parametros.put("TOTAL_SIN_SUBSIDIO", buscarValor(datos, "totalSinImpuestos"));
		parametros.put("AHORRO_POR_SUBSIDIO", buscarValor(datos, "ahorroPorSubsidio"));
		parametros.put("RAZON_MODIF", buscarValor(datos, "razonModificacion"));
		parametros.put("DOC_MODIFICADO", buscarValor(datos, "codDocModificado"));
		parametros.put("NUM_DOC_MODIFICADO", buscarValor(datos, "numDocModificado"));
		parametros.put("FECHA_EMISION_DOC_SUSTENTO", buscarValor(datos, "fechaEmisionDocSustento"));
		parametros.put("EJERCICIO_FISCAL", buscarValor(datos, "periodoFiscal"));

		parametros.put("IVA_12", buscarValor(datos, "iva12"));
		parametros.put("IVA_0", buscarValor(datos, "iva0"));
		parametros.put("TOTAL", buscarValor(datos, "importeTotal"));
		parametros.put("ICE", buscarValor(datos, "ice"));
		parametros.put("IVA", buscarValor(datos, "iva"));
		parametros.put("TOTAL_SIN_IMP", buscarValor(datos, "totalSinImpuestos"));
		parametros.put("NO_OBJETO_IVA", buscarValor(datos, "noObjetoIva"));
		parametros.put("EXENTO_IVA", buscarValor(datos, "exentoIva"));
		parametros.put("PORCENTAJE_IVA", buscarValor(datos, "porcentajeIva"));

		parametros.put("FECHA_INI_TRANSPORTE", buscarValor(datos, "fechaIniTransporte"));
		parametros.put("FECHA_FIN_TRANSPORTE", buscarValor(datos, "fechaFinTransporte"));
		parametros.put("RUC_TRANSPORTISTA", buscarValor(datos, "rucTransportista"));
		parametros.put("RS_TRANSPORTISTA", buscarValor(datos, "razonSocialTransportista"));
		parametros.put("PLACA", buscarValor(datos, "placa"));
		parametros.put("PUNTO_PARTIDA", buscarValor(datos, "puntoPartida"));

		parametros.put("INFO_ADICIONAL", obtenerLista(datos, "infoAdicional", "campoAdicional"));

		return parametros;
	}

	private JRDataSource crearDataSource(String nombrePlantilla, JsonNode datos, Map<String, Object> parametros) {
		JsonNode documento = obtenerDocumento(datos, nombrePlantilla);
		List<Map<String, Object>> filas;

		switch (nombrePlantilla) {
		case FACTURA, NOTA_CREDITO -> filas = obtenerFilas(documento, "detalles", "detalle");
		case NOTA_DEBITO -> filas = obtenerFilas(documento, "motivos", "motivo");
		case GUIA_REMISION -> filas = obtenerFilas(documento, "detalles", "detalle");
		case COMPROBANTE_RETENCION -> filas = obtenerFilas(documento, "impuestos", "impuesto");
		default -> filas = new ArrayList<>();
		}

		if (filas.isEmpty()) {
			filas.add(new LinkedHashMap<>());
		}

		Collection<Map<String, Object>> infoAdicional = obtenerLista(datos, "infoAdicional", "campoAdicional");
		Collection<Map<String, Object>> formasPago = construirFormasPago(documento);
		Collection<Map<String, Object>> totales = construirTotales(documento);

		for (Map<String, Object> fila : filas) {
			fila.putIfAbsent("infoAdicional", infoAdicional);
			fila.putIfAbsent("formasPago", formasPago);
			fila.putIfAbsent("totalesComprobante", totales);
		}

		parametros.put("INFO_ADICIONAL", infoAdicional);

		return new JRMapCollectionDataSource(filas);
	}

	private void compilarSubreportes(String nombrePlantilla, Path origen, Path destino,
			Map<String, Object> parametros) throws JRException {
		List<String> subreportes = switch (nombrePlantilla) {
		case FACTURA -> List.of("facturaInfoAdicional.jrxml", "facturaFormasPago.jrxml", "totalesComprobante.jrxml");
		case NOTA_CREDITO -> List.of("facturaInfoAdicional.jrxml", "totalesComprobante.jrxml");
		case NOTA_DEBITO -> List.of("facturaInfoAdicional.jrxml", "facturaFormasPago.jrxml",
				"totalesComprobante.jrxml");
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

	private JsonNode obtenerDocumento(JsonNode datos, String nombrePlantilla) {
		JsonNode documento = datos.get(nombrePlantilla);

		if (documento != null && documento.isObject()) {
			return documento;
		}

		if (datos.isObject()) {
			var iterator = datos.fields();
			while (iterator.hasNext()) {
				JsonNode candidato = iterator.next().getValue();
				if (candidato.isObject()) {
					return candidato;
				}
			}
		}

		return datos;
	}

	private List<Map<String, Object>> obtenerFilas(JsonNode documento, String contenedor, String lista) {
		JsonNode nodo = documento.path(contenedor).path(lista);
		if (!nodo.isArray()) {
			return new ArrayList<>();
		}

		return objectMapper.convertValue(nodo, new TypeReference<List<Map<String, Object>>>() {
		});
	}

	private Collection<Map<String, Object>> construirFormasPago(JsonNode documento) {
		JsonNode pagos = documento.path("infoFactura").path("pagos").path("pago");
		if (!pagos.isArray()) {
			pagos = documento.path("pagos").path("pago");
		}

		List<Map<String, Object>> resultado = new ArrayList<>();
		if (pagos.isArray()) {
			for (JsonNode pago : pagos) {
				Map<String, Object> fila = new LinkedHashMap<>();
				fila.put("formaPago", pago.path("formaPago").asText(null));
				fila.put("valor", pago.path("total").asText(null));
				resultado.add(fila);
			}
		}
		return resultado;
	}

	private Collection<Map<String, Object>> construirTotales(JsonNode documento) {
		JsonNode impuestos = documento.path("infoFactura").path("totalConImpuestos").path("totalImpuesto");
		if (!impuestos.isArray()) {
			impuestos = documento.path("totalConImpuestos").path("totalImpuesto");
		}

		List<Map<String, Object>> resultado = new ArrayList<>();
		if (impuestos.isArray()) {
			for (JsonNode impuesto : impuestos) {
				Map<String, Object> fila = new LinkedHashMap<>();
				fila.put("descripcion", impuesto.path("codigoPorcentaje").asText(null));
				fila.put("valor", impuesto.path("valor").asText(null));
				fila.put("esNegativo", false);
				resultado.add(fila);
			}
		}
		return resultado;
	}

	private Collection<Map<String, Object>> obtenerLista(JsonNode datos, String contenedor, String lista) {
		JsonNode nodo = datos.path(contenedor).path(lista);
		if (!nodo.isArray()) {
			return List.of();
		}
		return objectMapper.convertValue(nodo, new TypeReference<List<Map<String, Object>>>() {
		});
	}

	private Object buscarValor(JsonNode datos, String nombre) {
		if (datos == null || datos.isMissingNode()) {
			return null;
		}

		if (datos.isObject()) {
			var fields = datos.fields();
			while (fields.hasNext()) {
				var entry = fields.next();
				if (normalizarClave(entry.getKey()).equals(normalizarClave(nombre))) {
					return entry.getValue().isValueNode() ? entry.getValue().asText() : entry.getValue();
				}
				Object encontrado = buscarValor(entry.getValue(), nombre);
				if (encontrado != null) {
					return encontrado;
				}
			}
		}

		if (datos.isArray()) {
			for (JsonNode elemento : datos) {
				Object encontrado = buscarValor(elemento, nombre);
				if (encontrado != null) {
					return encontrado;
				}
			}
		}

		return null;
	}

	private Boolean buscarBoolean(JsonNode datos, String nombre) {
		Object valor = buscarValor(datos, nombre);
		if (valor == null) {
			return null;
		}
		if (valor instanceof Boolean booleano) {
			return booleano;
		}
		return Boolean.valueOf(String.valueOf(valor));
	}

	private String normalizarNombre(String nombre) {
		String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
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

	private String normalizarClave(String valor) {
		return valor == null ? "" : normalizarNombre(valor);
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
