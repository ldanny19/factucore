package ec.dalara.factucore.domain.comprobante;

import ec.dalara.factucore.domain.shared.DomainException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class GeneradorClaveAcceso {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    private static final int LONGITUD_CODIGO_NUMERICO = 8;

    private static final int MODULO = 11;

    private static final int PESO_INICIAL = 2;

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private GeneradorClaveAcceso() {
    }

    public static ClaveAcceso generar(
            LocalDate fechaEmision,
            String codigoDocumento,
            String ruc,
            String ambiente,
            String establecimiento,
            String puntoEmision,
            String secuencial,
            String tipoEmision
    ) {
        validar(
                fechaEmision,
                codigoDocumento,
                ruc,
                ambiente,
                establecimiento,
                puntoEmision,
                secuencial,
                tipoEmision
        );

        String fecha =
                fechaEmision.format(FORMATO_FECHA);

        String serie =
                establecimiento + puntoEmision;

        String codigoNumerico =
                generarCodigoNumerico();

        String base =
                fecha
                        + codigoDocumento
                        + ruc
                        + ambiente
                        + serie
                        + secuencial
                        + codigoNumerico
                        + tipoEmision;

        int digitoVerificador =
                calcularDigitoVerificador(base);

        return new ClaveAcceso(
                base + digitoVerificador
        );
    }

    static int calcularDigitoVerificador(
            String valor
    ) {
        if (valor == null
                || !valor.matches("\\d+")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.BASE.FORMATO_INVALIDO"
            );
        }

        int suma = 0;
        int peso = PESO_INICIAL;

        for (int i = valor.length() - 1; i >= 0; i--) {

            int digito =
                    Character.digit(
                            valor.charAt(i),
                            10
                    );

            suma += digito * peso;

            peso++;

            if (peso > 7) {
                peso = PESO_INICIAL;
            }
        }

        int residuo =
                suma % MODULO;

        return residuo == 0
                ? 0
                : MODULO - residuo;
    }

    private static String generarCodigoNumerico() {
        int valor =
                RANDOM.nextInt(100_000_000);

        return String.format(
                "%08d",
                valor
        );
    }

    private static void validar(
            LocalDate fechaEmision,
            String codigoDocumento,
            String ruc,
            String ambiente,
            String establecimiento,
            String puntoEmision,
            String secuencial,
            String tipoEmision
    ) {
        if (fechaEmision == null) {
            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.FECHA_EMISION.REQUERIDA"
            );
        }

        if (codigoDocumento == null
                || !codigoDocumento.matches("\\d{2}")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.CODIGO_DOCUMENTO.INVALIDO"
            );
        }

        if (ruc == null
                || !ruc.matches("\\d{13}")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.RUC.INVALIDO"
            );
        }

        if (ambiente == null
                || !ambiente.matches("\\d")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.AMBIENTE.INVALIDO"
            );
        }

        if (establecimiento == null
                || !establecimiento.matches("\\d{3}")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.ESTABLECIMIENTO.INVALIDO"
            );
        }

        if (puntoEmision == null
                || !puntoEmision.matches("\\d{3}")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.PUNTO_EMISION.INVALIDO"
            );
        }

        if (secuencial == null
                || !secuencial.matches("\\d{9}")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.SECUENCIAL.INVALIDO"
            );
        }

        if (tipoEmision == null
                || !tipoEmision.matches("\\d")) {

            throw new DomainException(
                    "FACTUCORE.CLAVE_ACCESO.TIPO_EMISION.INVALIDO"
            );
        }
    }
}