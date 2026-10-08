package ec.dalara.factucore.application.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentDefinitionDataValidator implements DocumentDefinitionValidator {

	private static final String VALOR_REQUERIDO = "FACTUCORE.COMPROBANTE.CAMPO.REQUERIDO";

	private static final String TIPO_INVALIDO = "FACTUCORE.COMPROBANTE.CAMPO.TIPO_INVALIDO";

	private static final String LONGITUD_MINIMA = "FACTUCORE.COMPROBANTE.CAMPO.LONGITUD_MINIMA";

	private static final String LONGITUD_MAXIMA = "FACTUCORE.COMPROBANTE.CAMPO.LONGITUD_MAXIMA";

	private static final String VALOR_MINIMO = "FACTUCORE.COMPROBANTE.CAMPO.VALOR_MINIMO";

	private static final String VALOR_MAXIMO = "FACTUCORE.COMPROBANTE.CAMPO.VALOR_MAXIMO";

	private static final String DIGITOS_TOTALES = "FACTUCORE.COMPROBANTE.CAMPO.DIGITOS_TOTALES";

	private static final String DECIMALES = "FACTUCORE.COMPROBANTE.CAMPO.DECIMALES";

	private static final String PATRON_INVALIDO = "FACTUCORE.COMPROBANTE.CAMPO.PATRON_INVALIDO";

	private static final String ENUMERACION_INVALIDA = "FACTUCORE.COMPROBANTE.CAMPO.ENUMERACION_INVALIDA";

	private static final String OCURRENCIAS_MINIMAS = "FACTUCORE.COMPROBANTE.CAMPO.OCURRENCIAS_MINIMAS";

	private static final String OCURRENCIAS_MAXIMAS = "FACTUCORE.COMPROBANTE.CAMPO.OCURRENCIAS_MAXIMAS";

	private final MessageResolver messageResolver;

	@Override
	public void validar(DocumentDefinitionModel definition, Map<String, Object> datos,
			ComprobanteValidationResult resultado) {
		if (definition == null || datos == null) {
			return;
		}

		Map<Long, ElementoXsdModel> elementosPorId = indexarElementos(definition.getElementos());
		Map<Long, MapeoXsdModel> mapeosPorElemento = indexarMapeosElemento(definition.getMapeos());
		Map<Long, AtributoXsdModel> atributosPorId = indexarAtributos(definition.getAtributos());
		Map<Long, List<AtributoXsdModel>> atributosPorElemento = agruparAtributosPorElemento(definition.getAtributos());
		Map<Long, List<ElementoXsdModel>> hijosPorElemento = agruparHijos(definition.getElementos());

		for (ElementoXsdModel elemento : definition.getElementos()) {
			if (elemento == null || elemento.getId() == null
					|| (elemento.getElementoPadreId() != null
							&& mapeosPorElemento.containsKey(elemento.getElementoPadreId()))
					|| !mapeosPorElemento.containsKey(elemento.getId())) {
				continue;
			}

			validarElemento(elemento, null, datos, mapeosPorElemento, elementosPorId, hijosPorElemento,
					atributosPorElemento, atributosPorId, definition, resultado);
		}
	}

	private void validarAtributo(AtributoXsdModel atributo, Object valor, String campo,
			ComprobanteValidationResult resultado) {
		/*
		 * Un atributo con valor predeterminado puede omitirse. El valor predeterminado
		 * será aplicado posteriormente durante la generación XML.
		 */
		if (valor == null) {

			if (atributo.getValorPredeterminado() != null) {
				return;
			}

			if (Boolean.TRUE.equals(atributo.getObligatorio())) {
				log.error("VALIDACION XSD - ATRIBUTO: codigo={}, tag={}, campo={}, valor={}",
						VALOR_REQUERIDO, atributo.getNombre(), campo, valor);
				resultado.agregarError(VALOR_REQUERIDO, campo);
			}

			return;
		}

		if (valor instanceof Collection<?> coleccion) {

			for (Object item : coleccion) {
				validarAtributo(atributo, item, campo, resultado);
			}

			return;
		}

		if (esEstructura(valor)) {
			return;
		}

		boolean tipoValido = validarTipo(atributo.getTipoDato(), valor, campo, resultado);

		if (!tipoValido) {
			return;
		}

		validarPatron(atributo.getPatron(), valor, campo, resultado);
	}

	private void validarElemento(ElementoXsdModel elemento, ElementoXsdModel padre, Object contexto,
			Map<Long, MapeoXsdModel> mapeosPorElemento, Map<Long, ElementoXsdModel> elementosPorId,
			Map<Long, List<ElementoXsdModel>> hijosPorElemento, Map<Long, List<AtributoXsdModel>> atributosPorElemento,
			Map<Long, AtributoXsdModel> atributosPorId, DocumentDefinitionModel definition,
			ComprobanteValidationResult resultado) {
		MapeoXsdModel mapeo = mapeosPorElemento.get(elemento.getId());

		if (mapeo == null || !"JSON".equals(mapeo.getTipoOrigen())) {
			return;
		}

		String rutaBase = padre == null ? null : obtenerOrigenElemento(padre, mapeosPorElemento);
		String rutaRelativa = rutaRelativa(mapeo.getOrigen(), rutaBase);
		boolean contenidoSimple = esContenidoSimple(elemento, mapeo);
		String rutaOcurrencia = contenidoSimple ? rutaOcurrencia(rutaRelativa, elemento.getNombre()) : rutaRelativa;

		Object valorOcurrencias = obtenerValorDirecto(contexto, rutaOcurrencia);
		Object valorValidacion = valorOcurrencias;

		if (contenidoSimple) {
			valorValidacion = obtenerContenidoSimple(valorOcurrencias, elemento.getNombre(), mapeo.getOrigen());
		}

		if (Boolean.TRUE.equals(elemento.getRepetible())) {
			if (!(valorOcurrencias instanceof Collection<?>)) {
				validarOcurrencias(elemento, valorOcurrencias == null ? 0 : 1, mapeo.getOrigen(), resultado);
				return;
			}

			Collection<?> lista = (Collection<?>) valorOcurrencias;
			validarOcurrencias(elemento, lista.size(), mapeo.getOrigen(), resultado);

			for (Object item : lista) {
				if (contenidoSimple) {
					validarValor(elemento, obtenerValorContenidoSimple(item, mapeo.getOrigen()),
						definition, mapeo.getOrigen(), resultado);
				} else {
					validarValor(elemento, item, definition, mapeo.getOrigen(), resultado);
				}

				validarAtributos(elemento, item, atributosPorElemento, atributosPorId, mapeosPorElemento,
						definition, resultado);

				validarHijos(elemento, item, hijosPorElemento, mapeosPorElemento, elementosPorId,
						atributosPorElemento, atributosPorId, definition, resultado);
			}
			return;
		}

		if (valorOcurrencias == null) {
			if (Boolean.TRUE.equals(elemento.getObligatorio())) {
				agregarRequerido(elemento, mapeo.getOrigen(), resultado);
			}
			return;
		}

		if (valorOcurrencias instanceof Collection<?>) {
			validarTipoInvalido(mapeo.getOrigen(), valorOcurrencias, elemento.getTipoDato(), resultado);
			return;
		}

		validarValor(elemento, valorValidacion, definition, mapeo.getOrigen(), resultado);
		validarAtributos(elemento, valorOcurrencias, atributosPorElemento, atributosPorId, mapeosPorElemento,
				definition, resultado);
		validarHijos(elemento, valorOcurrencias, hijosPorElemento, mapeosPorElemento, elementosPorId,
				atributosPorElemento, atributosPorId, definition, resultado);
	}

	private void validarHijos(ElementoXsdModel padre, Object contexto,
			Map<Long, List<ElementoXsdModel>> hijosPorElemento, Map<Long, MapeoXsdModel> mapeosPorElemento,
			Map<Long, ElementoXsdModel> elementosPorId, Map<Long, List<AtributoXsdModel>> atributosPorElemento,
			Map<Long, AtributoXsdModel> atributosPorId, DocumentDefinitionModel definition,
			ComprobanteValidationResult resultado) {
		if (!(contexto instanceof Map<?, ?>)) {
			return;
		}

		for (ElementoXsdModel hijo : hijosPorElemento.getOrDefault(padre.getId(), List.of())) {
			validarElemento(hijo, padre, contexto, mapeosPorElemento, elementosPorId, hijosPorElemento,
					atributosPorElemento, atributosPorId, definition, resultado);
		}
	}

	private void validarAtributos(ElementoXsdModel elemento, Object contexto,
			Map<Long, List<AtributoXsdModel>> atributosPorElemento, Map<Long, AtributoXsdModel> atributosPorId,
			Map<Long, MapeoXsdModel> mapeosPorElemento, DocumentDefinitionModel definition,
			ComprobanteValidationResult resultado) {
		if (!(contexto instanceof Map<?, ?> mapa)) {
			return;
		}

		for (AtributoXsdModel atributo : atributosPorElemento.getOrDefault(elemento.getId(), List.of())) {
			MapeoXsdModel mapeo = definition.getMapeos().stream()
					.filter(Objects::nonNull)
					.filter(m -> m.esAtributo() && Objects.equals(m.getAtributoXsdId(), atributo.getId()))
					.findFirst().orElse(null);

			if (mapeo == null || !"JSON".equals(mapeo.getTipoOrigen())) {
				continue;
			}

			String rutaRelativa = rutaRelativa(mapeo.getOrigen(), obtenerOrigenElemento(elemento, mapeosPorElemento));
			Object valor = obtenerValorDirecto(mapa, rutaRelativa);

			validarAtributo(atributo, valor, mapeo.getOrigen(), resultado);
		}
	}

	private Object obtenerValorDirecto(Object contexto, String ruta) {
		if (contexto == null) {
			return null;
		}
		if (ruta == null || ruta.isBlank()) {
			return contexto;
		}

		Object actual = contexto;
		for (String parte : ruta.split("\\.")) {
			if (!(actual instanceof Map<?, ?> mapa)) {
				return null;
			}
			actual = mapa.get(parte);
		}
		return actual;
	}

	private String rutaRelativa(String origen, String origenPadre) {
		if (origenPadre == null || origenPadre.isBlank()) {
			return origen;
		}
		String prefijo = origenPadre + ".";
		return origen.startsWith(prefijo) ? origen.substring(prefijo.length()) : origen;
	}

	private String obtenerOrigenElemento(ElementoXsdModel elemento, Map<Long, MapeoXsdModel> mapeosPorElemento) {
		MapeoXsdModel mapeo = mapeosPorElemento.get(elemento.getId());
		return mapeo == null ? null : mapeo.getOrigen();
	}

	private boolean esContenidoSimple(ElementoXsdModel elemento, MapeoXsdModel mapeo) {
		String[] partes = mapeo.getOrigen().split("\\.");
		return partes.length > 0 && elemento.getNombre().equals(partes[partes.length - 2]);
	}

	private String rutaOcurrencia(String ruta, String nombreElemento) {
		String sufijo = "." + nombreElemento + ".";
		int posicion = ruta.indexOf(sufijo);
		if (posicion >= 0) {
			return ruta.substring(0, posicion + sufijo.length() - 1);
		}
		if (ruta.equals(nombreElemento) || ruta.startsWith(nombreElemento + ".")) {
			int separador = ruta.indexOf('.');
			return separador < 0 ? nombreElemento : ruta.substring(0, separador);
		}
		return ruta;
	}

	private Object obtenerContenidoSimple(Object valor, String nombreElemento, String origen) {
		if (!(valor instanceof Collection<?> lista)) {
			return valor;
		}
		return lista.stream().map(item -> obtenerValorContenidoSimple(item, origen)).toList();
	}

	private Object obtenerValorContenidoSimple(Object item, String origen) {
		if (!(item instanceof Map<?, ?> mapa)) {
			return item;
		}
		String[] partes = origen.split("\\.");
		return mapa.get(partes[partes.length - 1]);
	}

	private void validarOcurrencias(ElementoXsdModel elemento, int ocurrencias, String campo,
			ComprobanteValidationResult resultado) {
		int minimo = elemento.getMinOcurrencias() == null ? 0 : elemento.getMinOcurrencias();

		if (ocurrencias < minimo) {
			log.error(
					"VALIDACION XSD - ELEMENTO: codigo={}, tag={}, campo={}, ocurrencias={}, minimo={}",
					OCURRENCIAS_MINIMAS, elemento.getNombre(), campo, ocurrencias, minimo);
			resultado.agregarError(OCURRENCIAS_MINIMAS, campo, minimo);
		}

		Integer maximo = elemento.getMaxOcurrencias();
		if (maximo != null && ocurrencias > maximo) {
			log.error(
					"VALIDACION XSD - ELEMENTO: codigo={}, tag={}, campo={}, ocurrencias={}, maximo={}",
					OCURRENCIAS_MAXIMAS, elemento.getNombre(), campo, ocurrencias, maximo);
			resultado.agregarError(OCURRENCIAS_MAXIMAS, campo, maximo);
		}
	}

	private void agregarRequerido(ElementoXsdModel elemento, String campo,
			ComprobanteValidationResult resultado) {
		log.error("VALIDACION XSD - ELEMENTO: codigo={}, tag={}, campo={}, valor={}",
				VALOR_REQUERIDO, elemento.getNombre(), campo, null);
		resultado.agregarError(VALOR_REQUERIDO, campo);
	}

	private void validarTipoInvalido(String campo, Object valor, String tipoDato,
			ComprobanteValidationResult resultado) {
		log.error("VALIDACION XSD - CAMPO: codigo={}, campo={}, valor={}, tipo={}",
				TIPO_INVALIDO, campo, valor, tipoDato);
		resultado.agregarError(TIPO_INVALIDO, campo, tipoDato);
	}

	private Map<Long, ElementoXsdModel> indexarElementos(List<ElementoXsdModel> elementos) {
		Map<Long, ElementoXsdModel> resultado = new LinkedHashMap<>();
		for (ElementoXsdModel elemento : elementos) {
			if (elemento != null && elemento.getId() != null) {
				resultado.put(elemento.getId(), elemento);
			}
		}
		return resultado;
	}

	private Map<Long, MapeoXsdModel> indexarMapeosElemento(List<MapeoXsdModel> mapeos) {
		Map<Long, MapeoXsdModel> resultado = new HashMap<>();
		for (MapeoXsdModel mapeo : mapeos) {
			if (mapeo != null && mapeo.esElemento() && mapeo.getElementoXsdId() != null) {
				resultado.put(mapeo.getElementoXsdId(), mapeo);
			}
		}
		return resultado;
	}

	private Map<Long, List<ElementoXsdModel>> agruparHijos(List<ElementoXsdModel> elementos) {
		Map<Long, List<ElementoXsdModel>> resultado = new HashMap<>();
		for (ElementoXsdModel elemento : elementos) {
			if (elemento != null && elemento.getElementoPadreId() != null) {
				resultado.computeIfAbsent(elemento.getElementoPadreId(), key -> new ArrayList<>()).add(elemento);
			}
		}
		return resultado;
	}

	private Map<Long, List<AtributoXsdModel>> agruparAtributosPorElemento(List<AtributoXsdModel> atributos) {
		Map<Long, List<AtributoXsdModel>> resultado = new HashMap<>();
		for (AtributoXsdModel atributo : atributos) {
			if (atributo != null && atributo.getElementoXsdId() != null) {
				resultado.computeIfAbsent(atributo.getElementoXsdId(), key -> new ArrayList<>()).add(atributo);
			}
		}
		return resultado;
	}


