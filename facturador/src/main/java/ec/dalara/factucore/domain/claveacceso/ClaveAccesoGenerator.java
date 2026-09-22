package ec.dalara.factucore.domain.claveacceso;

import ec.dalara.factucore.application.port.out.ClaveAccesoDatos;
import ec.dalara.factucore.domain.shared.DomainException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ClaveAccesoGenerator {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    private ClaveAccesoGenerator() {
    }

    public static ClaveAccesoModel generar(
            ClaveAccesoDatos datos
    ) {
        validar(datos);

        String fecha =
                datos.fechaEmision().format(FORMATO_FECHA);

        String codigoDocumento =
                normalizar(datos.codigoDocumento(), 2);

        String ruc =
                normalizarNumerico(datos.ruc(), 13);

        String ambiente =
                normalizarNumerico(datos.ambiente(), 1);

        String establecimiento =
                normalizarNumerico(datos.establecimiento(), 3);

        String puntoEmision =
                normalizarNumerico(datos.puntoEmision(), 3);

        String serie =
                establecimiento + puntoEmision;

        String secuencial =
                normalizarNumerico(datos.secuencial(), 9);

        String codigoNumerico =
                normalizarNumerico(datos.codigoNumerico(), 8);

        String tipoEmision =
                normalizarNumerico(datos.tipoEmision(), 1);

        String base =
                fecha
                        + codigoDocumento
                        + ruc
                        + ambiente
                        + serie
                        + secuencial
                        + codigoNumerico
                        + tipoEmision;

        String digito =
                calcularModulo11(base);

        String clave =
                base + digito;

        return new ClaveAccesoModel(
                clave,
                fecha,
                codigoDocumento,
                ruc,
                ambiente,
                serie,
                secuencial,
                codigoNumerico,
                tipoEmision,
                digito
        );
    }

    private static void validar(
            ClaveAccesoDatos datos
    ) {
        if (datos == null) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.DATOS.REQUERIDOS"
            );
        }

        if (datos.fechaEmision() == null) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.FECHA.REQUERIDA"
            );
        }
    }

    private static String normalizar(
            String valor,
            int longitud
    ) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.VALOR.REQUERIDO"
            );
        }

        String resultado = valor.trim();

        if (resultado.length() != longitud) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.LONGITUD.INVALIDA"
            );
        }

        return resultado;
    }

    private static String normalizarNumerico(
            String valor,
            int longitud
    ) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.VALOR.REQUERIDO"
            );
        }

        String resultado = valor.trim();

        if (!resultado.matches("\\d{" + longitud + "}")) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.FORMATO.NUMERICO_INVALIDO"
            );
        }

        return resultado;
    }

    private static String calcularModulo11(
            String valor
    ) {
        int factor = 2;
        int suma = 0;

        for (int i = valor.length() - 1; i >= 0; i--) {

            int digito =
                    Character.digit(
                            valor.charAt(i),
                            10
                    );

            suma += digito * factor;

            factor++;

            if (factor > 7) {
                factor = 2;
            }
        }

        int resultado =
                11 - (suma % 11);

        if (resultado == 11) {
            return "0";
        }

        if (resultado == 10) {
            return "1";
        }

        return String.valueOf(resultado);
    }
}