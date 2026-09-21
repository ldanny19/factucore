package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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

        for (ElementoXsdModel elemento : definition.getElementos()) {

            Object valor = obtenerValor(
                    datos,
                    elemento.getNombre()
            );

            String campo = elemento.getNombre();

            int ocurrencias = calcularOcurrencias(valor);

            int min = determinarMinimo(elemento);

            if (ocurrencias < min) {
                resultado.agregarError(
                        OCURRENCIAS_MINIMAS,
                        campo,
                        min
                );
                continue;
            }

            Integer max = elemento.getMaxOcurrencias();

            if (max != null && ocurrencias > max) {
                resultado.agregarError(
                        OCURRENCIAS_MAXIMAS,
                        campo,
                        max
                );
                continue;
            }

            if (valor == null) {
                continue;
            }

            if (Boolean.TRUE.equals(elemento.getRepetible())
                    && valor instanceof List<?> lista) {

                for (Object item : lista) {
                    validarValor(
                            elemento,
                            item,
                            definition,
                            campo,
                            resultado
                    );
                }

                continue;
            }

            validarValor(
                    elemento,
                    valor,
                    definition,
                    campo,
                    resultado
            );
        }
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

        validarTipo(
                elemento,
                valor,
                campo,
                resultado
        );

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
                elemento,
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

    private void validarTipo(
            ElementoXsdModel elemento,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        String tipo = normalizarTipo(elemento.getTipoDato());

        boolean valido = switch (tipo) {
            case "STRING", "TOKEN", "NORMALIZEDSTRING" ->
                    valor instanceof String;

            case "INTEGER", "LONG", "INT" ->
                    valor instanceof Integer
                            || valor instanceof Long;

            case "DECIMAL", "DOUBLE", "FLOAT" ->
                    valor instanceof Number
                            || valor instanceof BigDecimal;

            case "BOOLEAN" ->
                    valor instanceof Boolean;

            case "DATE", "DATETIME", "DATE_TIME" ->
                    valor instanceof String;

            default ->
                    true;
        };

        if (!valido) {
            resultado.agregarError(
                    TIPO_INVALIDO,
                    campo,
                    elemento.getTipoDato()
            );
        }
    }

    private void validarTexto(
            ElementoXsdModel elemento,
            String valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        Integer minimo = elemento.getLongitudMinima();

        if (minimo != null && valor.length() < minimo) {
            resultado.agregarError(
                    LONGITUD_MINIMA,
                    campo,
                    minimo
            );
        }

        Integer maximo = elemento.getLongitudMaxima();

        if (maximo != null && valor.length() > maximo) {
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
            numero = new BigDecimal(valor.toString());
        } catch (NumberFormatException e) {
            return;
        }

        if (elemento.getValorMinimo() != null
                && numero.compareTo(elemento.getValorMinimo()) < 0) {

            resultado.agregarError(
                    VALOR_MINIMO,
                    campo,
                    elemento.getValorMinimo()
            );
        }

        if (elemento.getValorMaximo() != null
                && numero.compareTo(elemento.getValorMaximo()) > 0) {

            resultado.agregarError(
                    VALOR_MAXIMO,
                    campo,
                    elemento.getValorMaximo()
            );
        }

        if (elemento.getDigitosTotales() != null) {

            int digitos = contarDigitos(numero);

            if (digitos > elemento.getDigitosTotales()) {
                resultado.agregarError(
                        DIGITOS_TOTALES,
                        campo,
                        elemento.getDigitosTotales()
                );
            }
        }

        if (elemento.getDecimales() != null) {

            int decimales = Math.max(
                    numero.stripTrailingZeros().scale(),
                    0
            );

            if (decimales > elemento.getDecimales()) {
                resultado.agregarError(
                        DECIMALES,
                        campo,
                        elemento.getDecimales()
                );
            }
        }
    }

    private void validarPatron(
            ElementoXsdModel elemento,
            Object valor,
            String campo,
            ComprobanteValidationResult resultado
    ) {
        String patron = elemento.getPatron();

        if (patron == null || patron.isBlank()) {
            return;
        }

        if (!(valor instanceof String texto)) {
            return;
        }

        try {
            if (!Pattern.matches(patron, texto)) {
                resultado.agregarError(
                        PATRON_INVALIDO,
                        campo
                );
            }
        } catch (Exception e) {
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

        String valorTexto = valor.toString();

        boolean existe = enumeraciones.stream()
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

    private Object obtenerValor(
            Map<String, Object> datos,
            String nombre
    ) {
        if (datos.containsKey(nombre)) {
            return datos.get(nombre);
        }

        return datos.get(nombre);
    }

    private int calcularOcurrencias(Object valor) {
        if (valor == null) {
            return 0;
        }

        if (valor instanceof List<?> lista) {
            return lista.size();
        }

        return 1;
    }

    private int determinarMinimo(
            ElementoXsdModel elemento
    ) {
        if (elemento.getMinOcurrencias() != null) {
            return elemento.getMinOcurrencias();
        }

        if (Boolean.TRUE.equals(elemento.getObligatorio())) {
            return 1;
        }

        return 0;
    }

    private boolean esNumerico(String tipoDato) {
        String tipo = normalizarTipo(tipoDato);

        return switch (tipo) {
            case "INTEGER", "LONG", "INT",
                 "DECIMAL", "DOUBLE", "FLOAT" -> true;
            default -> false;
        };
    }

    private String normalizarTipo(String tipoDato) {
        if (tipoDato == null) {
            return "";
        }

        String tipo = tipoDato.trim().toUpperCase();

        int separador = tipo.lastIndexOf(':');

        if (separador >= 0) {
            tipo = tipo.substring(separador + 1);
        }

        return tipo;
    }

    private int contarDigitos(BigDecimal numero) {
        return numero
                .abs()
                .stripTrailingZeros()
                .unscaledValue()
                .abs()
                .toString()
                .length();
    }
}