package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.documentoxsd.AtributoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.documentoxsd.MapeoXsdModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@RequiredArgsConstructor
public class DocumentDefinitionDataValidator
        implements DocumentDefinitionValidator {

    private static final String VALOR_REQUERIDO =
            "FACTUCORE.COMPROBANTE.CAMPO.REQUERIDO";

    private static final String TIPO_INVALIDO =
            "FACTUCORE.COMPROBANTE.CAMPO.TIPO_INVALIDO";

    private static final String LONGITUD_MINIMA =
            "FACTUCORE.COMPROBANTE.CAMPO.LONGITUD_MINIMA";

    private static final String LONGITUD_MAXIMA =
            "FACTUCORE.COMPROBANTE.CAMPO.LONGITUD_MAXIMA";

    private static final String VALOR_MINIMO =
            "FACTUCORE.COMPROBANTE.CAMPO.VALOR_MINIMO";

    private static final String VALOR_MAXIMO =
            "FACTUCORE.COMPROBANTE.CAMPO.VALOR_MAXIMO";

    private static final String DIGITOS_TOTALES =
            "FACTUCORE.COMPROBANTE.CAMPO.DIGITOS_TOTALES";

    private static final String DECIMALES =
            "FACTUCORE.COMPROBANTE.CAMPO.DECIMALES";

    private static final String PATRON_INVALIDO =
            "FACTUCORE.COMPROBANTE.CAMPO.PATRON_INVALIDO";

    private static final String ENUMERACION_INVALIDA =
            "FACTUCORE.COMPROBANTE.CAMPO.ENUMERACION_INVALIDA";

    private static final String OCURRENCIAS_MINIMAS =
            "FACTUCORE.COMPROBANTE.CAMPO.OCURRENCIAS_MINIMAS";

    private static final String OCURRENCIAS_MAXIMAS =
            "FACTUCORE.COMPROBANTE.CAMPO.OCURRENCIAS_MAXIMAS";

    private final MessageResolver messageResolver;

    @Override
    public void validar(
            DocumentDefinitionModel definition,
            Map<String, Object> datos,
            ComprobanteValidationResult resultado
    ) {
        if (definition == null || datos == null) {
            return;
        }

        Map<Long, ElementoXsdModel> elementosPorId =
                indexarElementos(
                        definition.getElementos()
                );

        for (MapeoXsdModel mapeo : definition.getMapeos()) {

            if (mapeo == null) {
                continue;
            }

            if (mapeo.esElemento()) {

                ElementoXsdModel elemento =
                        elementosPorId.get(
                                mapeo.getElementoXsdId()
                        );

                if (elemento == null) {
                    continue;
                }

                Object valor =
                        obtenerValor(
                                datos,
                                mapeo.getRutaOrigen()
                        );

                validarElemento(
                        elemento,
                        valor,
                        mapeo.getRutaOrigen(),
                        definition,
                        resultado
                );

                continue;
            }

            if (mapeo.esAtributo()) {

                validarAtributo(
                        mapeo,
                        datos,
                        definition,
                        elementosPorId,
                        resultado
                );
            }
        }
    }

    private void validarAtributo(
            MapeoXsdModel mapeo,
            Map<String, Object> datos,
            DocumentDefinitionModel definition,
            Map<Long, ElementoXsdModel> elementosPorId,
            ComprobanteValidationResult resultado
    ) {
        AtributoXsdModel atributo =
                encontrarAtributo(
                        mapeo,
                        definition
                );

        if (atributo == null) {
            return;
        }

        Object valor =
                obtenerValor(
                        datos,
                        mapeo.getRutaOrigen()
                );

        String campo =
                mapeo.getRutaOrigen();

        validarAtributoValor(
                atributo,
                valor,
                campo,
                resultado
        );
    }

    private AtributoXsdModel encontrarAtributo(
            MapeoXsdModel mapeo,
            DocumentDefinitionModel definition
    ) {
        if (mapeo.getAtributoXsdId() == null) {
            return null;
        }

        /*
         * AtributoXsdModel no posee un ID propio.
         * El identificador persistido del atributo corresponde
         * al elemento padre + nombre del atributo.
         *
         * Por eso se utiliza la referencia almacenada en
         * MapeoXsdModel únicamente para localizar el atributo
         * cuando el modelo disponible lo permite.
         *
         * La validación concreta se realiza contra la colección
         * de atributos de la definición.
         */

        return definition.getAtributos()
                .stream()
                .filter(Objects::nonNull)
                .filter(atributo ->
                        Objects.equals(
                                atributo.getElementoXsdId(),
                                obtenerElementoPadreId(
                                        mapeo,
                                        definition
                                )
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private Long obtenerElementoPadreId(
            MapeoXsdModel mapeo,
            DocumentDefinitionModel definition
    ) {
        return definition.getElementos()
                .stream()
                .filter(Objects::nonNull)
                .filter(elemento ->
                        Objects.equals(
                                elemento.getId(),
                                mapeo.getElementoXsdId()
                        )
                )
                .map(ElementoXsdModel::getId)
                .findFirst()
                .orElse(null);
    }

    private void validarAtributoValor(
            AtributoXsdModel atributo,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        if (valor == null) {

            if (Boolean.TRUE.equals(
                    atributo.getObligatorio()
            )) {

                resultado.agregarError(
                        VALOR_REQUERIDO,
                        campo
                );
            }

            return;
        }

        if (esEstructura(valor)) {
            return;
        }

        boolean tipoValido =
                validarTipo(
                        atributo.getTipoDato(),
                        valor,
                        campo,
                        resultado
                );

        if (!tipoValido) {
            return;
        }

        if (valor instanceof String texto) {

            validarLongitud(
                    texto,
                    null,
                    null,
                    campo,
                    resultado
            );
        }

        validarPatron(
                atributo.getPatron(),
                valor,
                campo,
                resultado
        );
    }

    private void validarElemento(
            ElementoXsdModel elemento,
            Object valor,
            String campo,
            DocumentDefinitionModel definition,
            ComprobanteValidationResult resultado
    ) {
        int ocurrencias =
                calcularOcurrencias(valor);

        int minimo =
                determinarMinimo(elemento);

        if (ocurrencias < minimo) {

            resultado.agregarError(
                    elemento.getObligatorio() != null
                            && elemento.getObligatorio()
                            ? VALOR_REQUERIDO
                            : OCURRENCIAS_MINIMAS,
                    campo,
                    minimo
            );

            return;
        }

        Integer maximo =
                elemento.getMaxOcurrencias();

        if (maximo != null
                && ocurrencias > maximo) {

            resultado.agregarError(
                    OCURRENCIAS_MAXIMAS,
                    campo,
                    maximo
            );

            return;
        }

        if (valor == null) {
            return;
        }

        if (valor instanceof Collection<?> coleccion) {

            for (Object item : coleccion) {

                validarValor(
                        elemento,
                        item,
                        definition,
                        campo,
                        resultado
                );
            }

            return;
        }

        validarValor(
                elemento,
                valor,
                definition,
                campo,
                resultado
        );
    }

    private void validarValor(
            ElementoXsdModel elemento,
            Object valor,
            DocumentDefinitionModel definition,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        if (valor == null) {
            return;
        }

        if (esEstructura(valor)) {
            return;
        }

        boolean tipoValido =
                validarTipo(
                        elemento,
                        valor,
                        campo,
                        resultado
                );

        if (!tipoValido) {
            return;
        }

        if (valor instanceof String texto) {

            validarTexto(
                    elemento,
                    texto,
                    campo,
                    resultado
            );
        }

        if (esNumerico(elemento.getTipoDato())) {

            validarNumerico(
                    elemento,
                    valor,
                    campo,
                    resultado
            );
        }

        validarPatron(
                elemento.getPatron(),
                valor,
                campo,
                resultado
        );

        validarEnumeracion(
                elemento,
                valor,
                definition,
                campo,
                resultado
        );
    }

    private boolean validarTipo(
            ElementoXsdModel elemento,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        return validarTipo(
                elemento.getTipoDato(),
                valor,
                campo,
                resultado
        );
    }

    private boolean validarTipo(
            String tipoDato,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        String tipo =
                normalizarTipo(tipoDato);

        boolean valido =
                switch (tipo) {

                    case "STRING",
                         "TOKEN",
                         "NORMALIZEDSTRING" ->
                            valor instanceof String;

                    case "INTEGER",
                         "INT",
                         "LONG",
                         "SHORT",
                         "BYTE" ->
                            valor instanceof Integer
                                    || valor instanceof Long
                                    || valor instanceof Short
                                    || valor instanceof Byte;

                    case "DECIMAL" ->
                            valor instanceof BigDecimal
                                    || valor instanceof Number
                                    || valor instanceof String;

                    case "DOUBLE",
                         "FLOAT" ->
                            valor instanceof Number;

                    case "BOOLEAN" ->
                            valor instanceof Boolean
                                    || esBooleanoTexto(valor);

                    case "DATE",
                         "DATETIME",
                         "DATE_TIME" ->
                            valor instanceof String;

                    default ->
                            true;
                };

        if (!valido) {

            resultado.agregarError(
                    TIPO_INVALIDO,
                    campo,
                    tipoDato
            );
        }

        return valido;
    }

    private void validarTexto(
            ElementoXsdModel elemento,
            String valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        validarLongitud(
                valor,
                elemento.getLongitudMinima(),
                elemento.getLongitudMaxima(),
                campo,
                resultado
        );
    }

    private void validarLongitud(
            String valor,
            Integer minimo,
            Integer maximo,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        if (minimo != null
                && valor.length() < minimo) {

            resultado.agregarError(
                    LONGITUD_MINIMA,
                    campo,
                    minimo
            );
        }

        if (maximo != null
                && valor.length() > maximo) {

            resultado.agregarError(
                    LONGITUD_MAXIMA,
                    campo,
                    maximo
            );
        }
    }

    private void validarNumerico(
            ElementoXsdModel elemento,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        BigDecimal numero;

        try {
            numero =
                    new BigDecimal(
                            valor.toString()
                    );
        } catch (NumberFormatException exception) {

            resultado.agregarError(
                    TIPO_INVALIDO,
                    campo,
                    elemento.getTipoDato()
            );

            return;
        }

        if (elemento.getValorMinimo() != null
                && numero.compareTo(
                        elemento.getValorMinimo()
                ) < 0) {

            resultado.agregarError(
                    VALOR_MINIMO,
                    campo,
                    elemento.getValorMinimo()
            );
        }

        if (elemento.getValorMaximo() != null
                && numero.compareTo(
                        elemento.getValorMaximo()
                ) > 0) {

            resultado.agregarError(
                    VALOR_MAXIMO,
                    campo,
                    elemento.getValorMaximo()
            );
        }

        if (elemento.getDigitosTotales() != null) {

            int digitos =
                    contarDigitos(numero);

            if (digitos >
                    elemento.getDigitosTotales()) {

                resultado.agregarError(
                        DIGITOS_TOTALES,
                        campo,
                        elemento.getDigitosTotales()
                );
            }
        }

        if (elemento.getDecimales() != null) {

            int decimales =
                    Math.max(
                            numero
                                    .stripTrailingZeros()
                                    .scale(),
                            0
                    );

            if (decimales >
                    elemento.getDecimales()) {

                resultado.agregarError(
                        DECIMALES,
                        campo,
                        elemento.getDecimales()
                );
            }
        }
    }

    private void validarPatron(
            String patron,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        if (patron == null
                || patron.isBlank()) {
            return;
        }

        if (!(valor instanceof String texto)) {
            return;
        }

        try {

            if (!Pattern.matches(
                    patron,
                    texto
            )) {

                resultado.agregarError(
                        PATRON_INVALIDO,
                        campo
                );
            }

        } catch (PatternSyntaxException exception) {

            resultado.agregarError(
                    PATRON_INVALIDO,
                    campo
            );
        }
    }

    private void validarEnumeracion(
            ElementoXsdModel elemento,
            Object valor,
            DocumentDefinitionModel definition,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        if (elemento.getId() == null) {
            return;
        }

        List<EnumeracionXsdModel> enumeraciones =
                definition.getEnumeraciones()
                        .stream()
                        .filter(enumeracion ->
                                Objects.equals(
                                        enumeracion.getElementoXsdId(),
                                        elemento.getId()
                                )
                        )
                        .toList();

        if (enumeraciones.isEmpty()) {
            return;
        }

        String valorTexto =
                valor.toString();

        boolean existe =
                enumeraciones.stream()
                        .anyMatch(enumeracion ->
                                Objects.equals(
                                        enumeracion.getValor(),
                                        valorTexto
                                )
                        );

        if (!existe) {

            resultado.agregarError(
                    ENUMERACION_INVALIDA,
                    campo,
                    valorTexto
            );
        }
    }

    private Map<Long, ElementoXsdModel> indexarElementos(
            List<ElementoXsdModel> elementos
    ) {
        Map<Long, ElementoXsdModel> resultado =
                new LinkedHashMap<>();

        for (ElementoXsdModel elemento : elementos) {

            if (elemento != null
                    && elemento.getId() != null) {

                resultado.put(
                        elemento.getId(),
                        elemento
                );
            }
        }

        return resultado;
    }

    private Object obtenerValor(
            Map<String, Object> datos,
            String ruta
    ) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        if (datos.containsKey(ruta)) {
            return datos.get(ruta);
        }

        String[] partes =
                ruta.split("\\.");

        Object actual = datos;

        for (String parte : partes) {

            if (!(actual instanceof Map<?, ?> mapa)) {
                return null;
            }

            actual =
                    mapa.get(parte);

            if (actual == null) {
                return null;
            }
        }

        return actual;
    }

    private int calcularOcurrencias(
            Object valor
    ) {
        if (valor == null) {
            return 0;
        }

        if (valor instanceof Collection<?> coleccion) {
            return coleccion.size();
        }

        return 1;
    }

    private int determinarMinimo(
            ElementoXsdModel elemento
    ) {
        if (elemento.getMinOcurrencias() != null) {
            return elemento.getMinOcurrencias();
        }

        if (Boolean.TRUE.equals(
                elemento.getObligatorio()
        )) {
            return 1;
        }

        return 0;
    }

    private boolean esNumerico(
            String tipoDato
    ) {
        String tipo =
                normalizarTipo(tipoDato);

        return switch (tipo) {
            case "INTEGER",
                 "INT",
                 "LONG",
                 "SHORT",
                 "BYTE",
                 "DECIMAL",
                 "DOUBLE",
                 "FLOAT" -> true;

            default -> false;
        };
    }

    private boolean esBooleanoTexto(
            Object valor
    ) {
        if (!(valor instanceof String texto)) {
            return false;
        }

        return "true".equalsIgnoreCase(texto)
                || "false".equalsIgnoreCase(texto);
    }

    private boolean esEstructura(
            Object valor
    ) {
        return valor instanceof Map<?, ?>
                || valor instanceof Collection<?>;
    }

    private String normalizarTipo(
            String tipoDato
    ) {
        if (tipoDato == null) {
            return "";
        }

        String tipo =
                tipoDato
                        .trim()
                        .toUpperCase();

        int separador =
                tipo.lastIndexOf(':');

        if (separador >= 0) {
            tipo =
                    tipo.substring(
                            separador + 1
                    );
        }

        return tipo;
    }

    private int contarDigitos(
            BigDecimal numero
    ) {
        BigDecimal absoluto =
                numero.abs()
                        .stripTrailingZeros();

        return absoluto
                .unscaledValue()
                .abs()
                .toString()
                .length();
    }
}