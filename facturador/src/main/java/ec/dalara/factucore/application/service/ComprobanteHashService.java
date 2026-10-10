package ec.dalara.factucore.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ec.dalara.factucore.application.contract.request.ComprobanteGeneracionRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteHashService {

	private final ObjectMapper objectMapper;

	@Value("${factucore.comprobantes.hash.identidad.campos:idEmpresa;idEstablecimiento;idPuntoEmision;idTipoDocumento;versionXsd;datos.factura.infoFactura.identificacionComprador}")
	private String camposIdentidad;

	@Value("${factucore.comprobantes.hash.contenido.campo:datos}")
	private String campoContenido;

	@Value("${factucore.comprobantes.hash.contenido.algoritmo:SHA-256}")
	private String algoritmo;

	public Hashes calcular(ComprobanteGeneracionRequest request) {
		List<String> campos = new ArrayList<>();
		for (String campo : camposIdentidad.split(";")) {
			String ruta = campo.trim();
			if (!ruta.isEmpty()) {
				campos.add(ruta + "=" + canonicalizar(resolver(ruta, request)));
			}
		}
		String identidad = hash(String.join("\n", campos));
		String rutaContenido = campoContenido == null || campoContenido.isBlank() ? "datos" : campoContenido.trim();
		String contenido = hash(canonicalizar(resolver(rutaContenido, request)));
		return new Hashes(identidad, contenido);
	}

	private JsonNode resolver(String ruta, ComprobanteGeneracionRequest request) {
		return switch (ruta) {
		case "idEmpresa" -> objectMapper.valueToTree(request.getIdEmpresa());
		case "idEstablecimiento" -> objectMapper.valueToTree(request.getIdEstablecimiento());
		case "idPuntoEmision" -> objectMapper.valueToTree(request.getIdPuntoEmision());
		case "idTipoDocumento" -> objectMapper.valueToTree(request.getIdTipoDocumento());
		case "versionXsd" -> objectMapper.valueToTree(request.getVersionXsd());
		case "idDocumentoOrigen" -> objectMapper.valueToTree(request.getIdDocumentoOrigen());
		case "datos" -> request.getDatos();
		default -> resolverDatos(ruta, request);
		};
	}

	private JsonNode resolverDatos(String ruta, ComprobanteGeneracionRequest request) {
		if (request.getDatos() == null || !ruta.startsWith("datos.")) {
			return JsonNodeFactory.instance.nullNode();
		}
		JsonNode nodo = request.getDatos();
		for (String segmento : ruta.substring("datos.".length()).split("\\.")) {
			nodo = nodo == null ? null : nodo.get(segmento);
			if (nodo == null) {
				return JsonNodeFactory.instance.nullNode();
			}
		}
		return nodo;
	}

	private String canonicalizar(JsonNode nodo) {
		try {
			return objectMapper.writeValueAsString(ordenar(nodo));
		} catch (Exception exception) {
			throw new IllegalStateException("No se pudo normalizar el JSON del comprobante", exception);
		}
	}

	private JsonNode ordenar(JsonNode nodo) {
		if (nodo == null || nodo.isNull() || nodo.isValueNode()) {
			return nodo == null ? JsonNodeFactory.instance.nullNode() : nodo;
		}
		if (nodo.isArray()) {
			var arreglo = JsonNodeFactory.instance.arrayNode();
			nodo.forEach(elemento -> arreglo.add(ordenar(elemento)));
			return arreglo;
		}
		ObjectNode objeto = JsonNodeFactory.instance.objectNode();
		List<String> nombres = new ArrayList<>();
		nodo.fieldNames().forEachRemaining(nombres::add);
		nombres.sort(Comparator.naturalOrder());
		for (String nombre : nombres) {
			objeto.set(nombre, ordenar(nodo.get(nombre)));
		}
		return objeto;
	}

	private String hash(String valor) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algoritmo);
			return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("Algoritmo de hash no soportado: " + algoritmo, exception);
		}
	}

	public record Hashes(String identidad, String contenido) {
	}
}
