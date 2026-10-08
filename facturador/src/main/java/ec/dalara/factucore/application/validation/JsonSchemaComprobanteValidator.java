package ec.dalara.factucore.application.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonSchemaComprobanteValidator {

	private static final Pattern REQUIRED_PROPERTY = Pattern.compile(
			"required property ['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);

	private final ObjectMapper objectMapper;
	private final MessageResolver messageResolver;

	public void validar(String esquemaJson, JsonNode datos, ComprobanteValidationResult resultado) {
		if (datos == null || datos.isNull()) {
			return;
		}

		try {
			JsonNode esquema = objectMapper.readTree(esquemaJson);
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
			JsonSchema jsonSchema = factory.getSchema(esquema);
			for (ValidationMessage error : jsonSchema.validate(datos)) {
				String ruta = normalizarRuta(error.getPath());
				String propiedadRequerida = obtenerPropiedadRequerida(error.getMessage());
				if (propiedadRequerida != null) {
					resultado.agregarError(MessageCodes.COMPROBANTE_CAMPO_REQUERIDO,
							propiedadRequerida, propiedadRequerida);
				} else {
					resultado.agregarError(MessageCodes.COMPROBANTE_JSON_SCHEMA_INVALIDO, ruta,
							ruta, error.getMessage());
				}
			}
		} catch (Exception exception) {
			resultado.agregarError(MessageCodes.COMPROBANTE_ESQUEMA_JSON_INVALIDO, "esquemaJson");
		}
	}

	private String normalizarRuta(String ruta) {
		if (ruta == null || ruta.isBlank() || "$".equals(ruta)) {
			return "datos";
		}
		return ruta.startsWith("$.") ? ruta.substring(2) : ruta;
	}

	private String obtenerPropiedadRequerida(String mensaje) {
		if (mensaje == null) {
			return null;
		}
		Matcher matcher = REQUIRED_PROPERTY.matcher(mensaje);
		return matcher.find() ? matcher.group(1) : null;
	}
}
