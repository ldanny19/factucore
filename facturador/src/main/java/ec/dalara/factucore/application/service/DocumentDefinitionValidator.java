package ec.dalara.factucore.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import ec.dalara.factucore.domain.documentoxsd.DocumentDefinitionModel;
import ec.dalara.factucore.domain.documentoxsd.ElementoXsdModel;
import ec.dalara.factucore.domain.documentoxsd.EnumeracionXsdModel;
import ec.dalara.factucore.domain.validacion.ValidationResult;
import ec.dalara.factucore.domain.validacion.ValidationViolation;

@Service
public class DocumentDefinitionValidator {

    public ValidationResult validar(Map<String, Object> datos, DocumentDefinitionModel definition) {
        List<ValidationViolation> errores = new ArrayList<>();
        for (ElementoXsdModel elemento : definition.getElementos()) {
            if (elemento.getElementoPadreId() != null) continue;
            validarElemento(elemento, datos, definition, errores);
        }
        return new ValidationResult(errores);
    }

    private void validarElemento(ElementoXsdModel elemento, Map<String, Object> datos,
            DocumentDefinitionModel definition, List<ValidationViolation> errores) {
        Object valor = datos.get(elemento.getNombre());
        validarValor(elemento, valor, definition, errores);
    }

    private void validarValor(ElementoXsdModel elemento, Object valor,
            DocumentDefinitionModel definition, List<ValidationViolation> errores) {
        int ocurrencias = cantidad(valor);
        int min = elemento.getMinOcurrencias() == null ? 0 : elemento.getMinOcurrencias();
        Integer max = elemento.getMaxOcurrencias();

        if (ocurrencias < min) {
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.OCURRENCIA.MINIMA",
                    elemento.getNombre(), min));
            return;
        }
        if (max != null && ocurrencias > max) {
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.OCURRENCIA.MAXIMA",
                    elemento.getNombre(), max));
            return;
        }

        if (valor instanceof Collection<?> collection) {
            for (Object item : collection) validarEscalar(elemento, item, definition, errores);
        } else if (valor != null) {
            validarEscalar(elemento, valor, definition, errores);
        }
    }

    private void validarEscalar(ElementoXsdModel elemento, Object valor,
            DocumentDefinitionModel definition, List<ValidationViolation> errores) {
        String texto = String.valueOf(valor);

        if (elemento.getLongitudMinima() != null && texto.length() < elemento.getLongitudMinima())
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.LONGITUD.MINIMA", elemento.getNombre(), elemento.getLongitudMinima()));
        if (elemento.getLongitudMaxima() != null && texto.length() > elemento.getLongitudMaxima())
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.LONGITUD.MAXIMA", elemento.getNombre(), elemento.getLongitudMaxima()));

        if (elemento.getPatron() != null && !Pattern.matches(elemento.getPatron(), texto))
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.PATRON.INVALIDO", elemento.getNombre()));

        BigDecimal numero = numero(valor);
        if (numero != null) {
            if (elemento.getValorMinimo() != null && numero.compareTo(elemento.getValorMinimo()) < 0)
                errores.add(new ValidationViolation("FACTUCORE.VALIDACION.VALOR.MINIMO", elemento.getNombre(), elemento.getValorMinimo()));
            if (elemento.getValorMaximo() != null && numero.compareTo(elemento.getValorMaximo()) > 0)
                errores.add(new ValidationViolation("FACTUCORE.VALIDACION.VALOR.MAXIMO", elemento.getNombre(), elemento.getValorMaximo()));
            if (elemento.getDigitosTotales() != null && digitosTotales(numero) > elemento.getDigitosTotales())
                errores.add(new ValidationViolation("FACTUCORE.VALIDACION.DIGITOS.TOTALES", elemento.getNombre(), elemento.getDigitosTotales()));
            if (elemento.getDecimales() != null && Math.max(0, numero.stripTrailingZeros().scale()) > elemento.getDecimales())
                errores.add(new ValidationViolation("FACTUCORE.VALIDACION.DECIMALES", elemento.getNombre(), elemento.getDecimales()));
        }

        List<EnumeracionXsdModel> enumeraciones = definition.getEnumeraciones().stream()
                .filter(e -> elemento.getId().equals(e.getElementoXsdId())).toList();
        if (!enumeraciones.isEmpty() && enumeraciones.stream().noneMatch(e -> e.getValor().equals(texto)))
            errores.add(new ValidationViolation("FACTUCORE.VALIDACION.ENUMERACION.INVALIDA", elemento.getNombre()));
    }

    private static int cantidad(Object valor) {
        if (valor == null) return 0;
        if (valor instanceof Collection<?> collection) return collection.size();
        return 1;
    }

    private static BigDecimal numero(Object valor) {
        if (valor instanceof BigDecimal decimal) return decimal;
        if (valor instanceof Number number) return new BigDecimal(number.toString());
        try { return new BigDecimal(String.valueOf(valor)); } catch (NumberFormatException ignored) { return null; }
    }

    private static int digitosTotales(BigDecimal numero) {
        return numero.unscaledValue().abs().toString().length();
    }
}
