package ec.dalara.factucore.application.validation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
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

    private static final String REQUERIDO = "FACTUCORE.COMPROBANTE.CAMPO.REQUERIDO";
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

        Map<Long, ElementoXsdModel> elementos = new HashMap<>();
        Map<Long, List<ElementoXsdModel>> hijos = new HashMap<>();
        Map<Long, List<AtributoXsdModel>> atributos = new HashMap<>();
        Map<Long, List<MapeoXsdModel>> mapeos = new HashMap<>();

        for (ElementoXsdModel elemento : definition.getElementos()) {
            elementos.put(elemento.getId(), elemento);
            if (elemento.getElementoPadreId() != null) {
                hijos.computeIfAbsent(elemento.getElementoPadreId(), key -> new java.util.ArrayList<>()).add(elemento);
            }
        }
        for (AtributoXsdModel atributo : definition.getAtributos()) {
            atributos.computeIfAbsent(atributo.getElementoXsdId(), key -> new java.util.ArrayList<>()).add(atributo);
        }
        for (MapeoXsdModel mapeo : definition.getMapeos()) {
            if (mapeo != null && mapeo.getElementoXsdId() != null) {
                mapeos.computeIfAbsent(mapeo.getElementoXsdId(), key -> new java.util.ArrayList<>()).add(mapeo);
            }
        }

        for (ElementoXsdModel raiz : definition.getElementos().stream()
                .filter(e -> e.getElementoPadreId() == null)
                .sorted((a, b) -> Integer.compare(
                        a.getOrden() == null ? Integer.MAX_VALUE : a.getOrden(),
                        b.getOrden() == null ? Integer.MAX_VALUE : b.getOrden()))
                .toList()) {
            validarElemento(raiz, datos, raiz.getNombre(), definition, elementos, hijos, atributos, mapeos, resultado);
        }
    }

    private void validarElemento(ElementoXsdModel elemento, Object contexto, String campo,
            DocumentDefinitionModel definition, Map<Long, ElementoXsdModel> elementos,
            Map<Long, List<ElementoXsdModel>> hijos, Map<Long, List<AtributoXsdModel>> atributos,
            Map<Long, List<MapeoXsdModel>> mapeos, ComprobanteValidationResult resultado) {

        Object valor = resolverValorElemento(elemento, contexto, definition, rutaPadre(campo, elemento), mapeos);

        if (Boolean.TRUE.equals(elemento.getRepetible())) {
            if (!(valor instanceof Collection<?> lista)) {
                validarOcurrencias(elemento, valor == null ? 0 : 1, campo, resultado);
                return;
            }

            validarOcurrencias(elemento, lista.size(), campo, resultado);
            for (Object item : lista) {
                validarContenidoElemento(elemento, item, campo, definition, elementos, hijos, atributos, mapeos,
                        resultado);
            }
            return;
        }

        if (valor == null) {
            if (Boolean.TRUE.equals(elemento.getObligatorio())) {
                resultado.agregarError(REQUERIDO, campo);
            }
            return;
        }

        validarContenidoElemento(elemento, valor, campo, definition, elementos, hijos, atributos, mapeos, resultado);
    }

    private void validarContenidoElemento(ElementoXsdModel elemento, Object valor, String campo,
            DocumentDefinitionModel definition, Map<Long, ElementoXsdModel> elementos,
            Map<Long, List<ElementoXsdModel>> hijos, Map<Long, List<AtributoXsdModel>> atributos,
            Map<Long, List<MapeoXsdModel>> mapeos, ComprobanteValidationResult resultado) {

        boolean tieneHijos = !hijos.getOrDefault(elemento.getId(), List.of()).isEmpty();

        if (tieneHijos) {
            if (!(valor instanceof Map<?, ?>)) {
                resultado.agregarError(TIPO_INVALIDO, campo, elemento.getTipoDato());
                return;
            }

            validarAtributos(elemento, valor, campo, atributos, definition, mapeos, resultado);
            for (ElementoXsdModel hijo : hijos.getOrDefault(elemento.getId(), List.of())) {
                validarElemento(hijo, valor, campo + "." + hijo.getNombre(), definition, elementos, hijos,
                        atributos, mapeos, resultado);
            }
            return;
        }

        if (esContenidoSimple(elemento, definition, mapeos)) {
            Object contenido = obtenerContenidoSimple(valor, elemento, definition, mapeos);
            if (contenido == null) {
                if (Boolean.TRUE.equals(elemento.getObligatorio())) {
                    resultado.agregarError(REQUERIDO, campo);
                }
            } else {
                validarValor(elemento, contenido, campo, definition, resultado);
            }
        } else {
            validarValor(elemento, valor, campo, definition, resultado);
        }

        validarAtributos(elemento, valor, campo, atributos, definition, mapeos, resultado);
    }

    private Object resolverValorElemento(ElementoXsdModel elemento, Object contexto,
            DocumentDefinitionModel definition, String rutaPadre, Map<Long, List<MapeoXsdModel>> mapeos) {
        List<MapeoXsdModel> propios = mapeos.getOrDefault(elemento.getId(), List.of());
        for (MapeoXsdModel mapeo : propios) {
            if ("JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
                Object valor = obtenerRuta(contexto, rutaRelativa(mapeo.getOrigen(), rutaPadre));
                if (valor == null && (rutaPadre == null || rutaPadre.isBlank())) {
                    valor = obtenerRuta(contexto, mapeo.getOrigen());
                }
                return valor;
            }
        }

        String ruta = rutaJsonElemento(elemento, definition, mapeos);
        return ruta == null ? contexto : obtenerRuta(contexto, ruta);
    }

    private String rutaPadre(String campo, ElementoXsdModel elemento) {
        if (campo == null || elemento == null || elemento.getNombre() == null) {
            return null;
        }
        int posicion = campo.lastIndexOf("." + elemento.getNombre());
        return posicion < 0 ? null : campo.substring(0, posicion);
    }

    private String rutaJsonElemento(ElementoXsdModel elemento, DocumentDefinitionModel definition,
            Map<Long, List<MapeoXsdModel>> mapeos) {
        for (MapeoXsdModel mapeo : definition.getMapeos()) {
            if (mapeo == null || !"JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
                continue;
            }
            Long destino = mapeo.esElemento() ? mapeo.getElementoXsdId() : elementoDeAtributo(mapeo, definition);
            if (!esDescendiente(destino, elemento.getId(), definition)) {
                continue;
            }
            String ruta = recortarRuta(mapeo.getOrigen(), elemento.getNombre());
            if (ruta != null) {
                return ruta;
            }
        }
        return null;
    }

    private Object obtenerRuta(Object datos, String ruta) {
        if (datos == null || ruta == null || ruta.isBlank()) {
            return datos;
        }
        Object actual = datos;
        for (String parte : ruta.split("\\.")) {
            if (!(actual instanceof Map<?, ?> mapa)) {
                return null;
            }
            actual = mapa.get(parte);
        }
        return actual;
    }

    private String rutaRelativa(String origen, String padre) {
        if (padre == null || padre.isBlank()) {
            return origen;
        }
        String prefijo = padre + ".";
        return origen.startsWith(prefijo) ? origen.substring(prefijo.length()) : origen;
    }

    private String recortarRuta(String ruta, String nombre) {
        String token = "." + nombre;
        int pos = ruta.lastIndexOf(token);
        return pos >= 0 ? ruta.substring(0, pos + token.length()) : (ruta.equals(nombre) ? nombre : null);
    }

    private boolean esDescendiente(Long destino, Long ancestro, DocumentDefinitionModel definition) {
        ElementoXsdModel actual = buscarElemento(destino, definition);
        while (actual != null) {
            if (Objects.equals(actual.getId(), ancestro)) {
                return true;
            }
            actual = buscarElemento(actual.getElementoPadreId(), definition);
        }
        return false;
    }

    private Long elementoDeAtributo(MapeoXsdModel mapeo, DocumentDefinitionModel definition) {
        if (mapeo.getAtributoXsdId() == null) {
            return null;
        }
        return definition.getAtributos().stream()
                .filter(a -> Objects.equals(a.getId(), mapeo.getAtributoXsdId()))
                .map(AtributoXsdModel::getElementoXsdId)
                .findFirst().orElse(null);
    }

    private ElementoXsdModel buscarElemento(Long id, DocumentDefinitionModel definition) {
        if (id == null) {
            return null;
        }
        return definition.getElementos().stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst().orElse(null);
    }

    private boolean esContenidoSimple(ElementoXsdModel elemento, DocumentDefinitionModel definition,
            Map<Long, List<MapeoXsdModel>> mapeos) {
        return !definition.getElementos().stream()
                .anyMatch(e -> Objects.equals(e.getElementoPadreId(), elemento.getId()))
                && mapeos.getOrDefault(elemento.getId(), List.of()).stream().anyMatch(MapeoXsdModel::esElemento);
    }

    private Object obtenerContenidoSimple(Object valor, ElementoXsdModel elemento,
            DocumentDefinitionModel definition, Map<Long, List<MapeoXsdModel>> mapeos) {
        if (!(valor instanceof Map<?, ?> mapa)) {
            return valor;
        }
        MapeoXsdModel mapeo = mapeos.getOrDefault(elemento.getId(), List.of()).stream()
                .filter(MapeoXsdModel::esElemento)
                .findFirst().orElse(null);
        if (mapeo == null || mapeo.getOrigen() == null) {
            return null;
        }
        int pos = mapeo.getOrigen().lastIndexOf('.');
        return mapa.get(pos < 0 ? mapeo.getOrigen() : mapeo.getOrigen().substring(pos + 1));
    }

    private void validarAtributos(ElementoXsdModel elemento, Object valor, String campo,
            Map<Long, List<AtributoXsdModel>> atributos, DocumentDefinitionModel definition,
            Map<Long, List<MapeoXsdModel>> mapeos, ComprobanteValidationResult resultado) {
        if (!(valor instanceof Map<?, ?> mapa)) {
            return;
        }

        for (AtributoXsdModel atributo : atributos.getOrDefault(elemento.getId(), List.of())) {
            MapeoXsdModel mapeo = definition.getMapeos().stream()
                    .filter(Objects::nonNull)
                    .filter(MapeoXsdModel::esAtributo)
                    .filter(m -> Objects.equals(m.getAtributoXsdId(), atributo.getId()))
                    .findFirst().orElse(null);
            if (mapeo == null || !"JSON".equalsIgnoreCase(mapeo.getTipoOrigen())) {
                continue;
            }

            int pos = mapeo.getOrigen().lastIndexOf('.');
            String nombre = pos < 0 ? mapeo.getOrigen() : mapeo.getOrigen().substring(pos + 1);
            Object valorAtributo = mapa.get(nombre);
            if (valorAtributo == null) {
                if (Boolean.TRUE.equals(atributo.getObligatorio()) && atributo.getValorPredeterminado() == null) {
                    resultado.agregarError(REQUERIDO, campo + "." + atributo.getNombre());
                }
            } else {
                validarTipoYPatron(atributo.getTipoDato(), atributo.getPatron(), valorAtributo,
                        campo + "." + atributo.getNombre(), resultado);
            }
        }
    }

    private void validarValor(ElementoXsdModel elemento, Object valor, String campo,
            DocumentDefinitionModel definition, ComprobanteValidationResult resultado) {
        validarTipoYPatron(elemento.getTipoDato(), elemento.getPatron(), valor, campo, resultado);

        if (valor == null) {
            return;
        }

        String texto = String.valueOf(valor);
        if (elemento.getLongitudMinima() != null && texto.length() < elemento.getLongitudMinima()) {
            resultado.agregarError(LONGITUD_MINIMA, campo, elemento.getLongitudMinima());
        }
        if (elemento.getLongitudMaxima() != null && texto.length() > elemento.getLongitudMaxima()) {
            resultado.agregarError(LONGITUD_MAXIMA, campo, elemento.getLongitudMaxima());
        }

        try {
            BigDecimal numero = new BigDecimal(texto);
            if (elemento.getValorMinimo() != null && numero.compareTo(elemento.getValorMinimo()) < 0) {
                resultado.agregarError(VALOR_MINIMO, campo, elemento.getValorMinimo());
            }
            if (elemento.getValorMaximo() != null && numero.compareTo(elemento.getValorMaximo()) > 0) {
                resultado.agregarError(VALOR_MAXIMO, campo, elemento.getValorMaximo());
            }
            if (elemento.getDigitosTotales() != null && contarDigitos(texto) > elemento.getDigitosTotales()) {
                resultado.agregarError(DIGITOS_TOTALES, campo, elemento.getDigitosTotales());
            }
            if (elemento.getDecimales() != null && escala(texto) > elemento.getDecimales()) {
                resultado.agregarError(DECIMALES, campo, elemento.getDecimales());
            }
        } catch (NumberFormatException exception) {
            // Las restricciones numéricas solo aplican cuando el valor es numérico.
        }

        validarEnumeracion(elemento, texto, definition.getEnumeraciones(), campo, resultado);
    }

    private void validarTipoYPatron(String tipo, String patron, Object valor, String campo,
            ComprobanteValidationResult resultado) {
        if (valor == null) {
            return;
        }
        if ("MAP".equalsIgnoreCase(tipo) && !(valor instanceof Map<?, ?>)) {
            resultado.agregarError(TIPO_INVALIDO, campo, tipo);
            return;
        }
        if ("LIST".equalsIgnoreCase(tipo) && !(valor instanceof Collection<?>)) {
            resultado.agregarError(TIPO_INVALIDO, campo, tipo);
            return;
        }
        if (("MAP".equalsIgnoreCase(tipo) || "LIST".equalsIgnoreCase(tipo))) {
            return;
        }
        if (valor instanceof Map<?, ?> || valor instanceof Collection<?>) {
            resultado.agregarError(TIPO_INVALIDO, campo, tipo);
            return;
        }

        if (patron != null && !patron.isBlank()) {
            try {
                if (!Pattern.compile(patron).matcher(String.valueOf(valor)).matches()) {
                    resultado.agregarError(PATRON_INVALIDO, campo, patron);
                }
            } catch (PatternSyntaxException exception) {
                resultado.agregarError(PATRON_INVALIDO, campo, patron);
            }
        }
    }

    private void validarEnumeracion(ElementoXsdModel elemento, String valor,
            List<EnumeracionXsdModel> enumeraciones, String campo, ComprobanteValidationResult resultado) {
        List<EnumeracionXsdModel> propias = enumeraciones.stream()
                .filter(e -> Objects.equals(e.getElementoXsdId(), elemento.getId()))
                .toList();
        if (!propias.isEmpty() && propias.stream().noneMatch(e -> Objects.equals(e.getValor(), valor))) {
            resultado.agregarError(ENUMERACION_INVALIDA, campo, valor);
        }
    }

    private int contarDigitos(String valor) {
        return (int) valor.chars().filter(Character::isDigit).count();
    }

    private int escala(String valor) {
        int punto = valor.indexOf('.');
        return punto < 0 ? 0 : valor.length() - punto - 1;
    }

    private void validarOcurrencias(ElementoXsdModel elemento, int ocurrencias, String campo,
            ComprobanteValidationResult resultado) {
        int minimo = elemento.getMinOcurrencias() == null ? 0 : elemento.getMinOcurrencias();
        if (ocurrencias < minimo) {
            resultado.agregarError(OCURRENCIAS_MINIMAS, campo, minimo);
        }
        Integer maximo = elemento.getMaxOcurrencias();
        if (maximo != null && ocurrencias > maximo) {
            resultado.agregarError(OCURRENCIAS_MAXIMAS, campo, maximo);
        }
    }
}