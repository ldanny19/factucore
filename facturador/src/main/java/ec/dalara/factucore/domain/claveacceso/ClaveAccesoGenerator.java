package ec.dalara.factucore.domain.claveacceso;

import java.time.format.DateTimeFormatter;
import ec.dalara.factucore.domain.shared.DomainException;

public final class ClaveAccesoGenerator {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final int LONGITUD_BASE = 48;

    private ClaveAccesoGenerator() {}

    public static ClaveAccesoModel generar(ClaveAccesoDatos datos) {
        validar(datos);
        String base = datos.fechaEmision().format(FORMATO_FECHA)
                + numerico(datos.codigoDocumento(), 2)
                + numerico(datos.ruc(), 13)
                + numerico(datos.ambiente(), 1)
                + numerico(datos.establecimiento(), 3)
                + numerico(datos.puntoEmision(), 3)
                + numerico(datos.secuencial(), 9)
                + numerico(datos.codigoNumerico(), 8)
                + numerico(datos.tipoEmision(), 1);
        if (base.length() != LONGITUD_BASE) {
            throw new DomainException("FACTUCORE.CLAVE_ACCESO.BASE.LONGITUD_INVALIDA");
        }
        String dv = calcularModulo11(base);
        return new ClaveAccesoModel(base + dv, datos.fechaEmision().format(FORMATO_FECHA),
                datos.codigoDocumento(), datos.ruc(), datos.ambiente(),
                datos.establecimiento() + datos.puntoEmision(), datos.secuencial(),
                datos.codigoNumerico(), datos.tipoEmision(), dv);
    }

    private static void validar(ClaveAccesoDatos datos) {
        if (datos == null) throw new DomainException("FACTUCORE.CLAVE_ACCESO.DATOS.REQUERIDOS");
        if (datos.fechaEmision() == null) throw new DomainException("FACTUCORE.CLAVE_ACCESO.FECHA.REQUERIDA");
    }

    private static String numerico(String valor, int longitud) {
        if (valor == null || valor.isBlank()) throw new DomainException("FACTUCORE.CLAVE_ACCESO.VALOR.REQUERIDO");
        String resultado = valor.trim();
        if (!resultado.matches("\\d{" + longitud + "}"))
            throw new DomainException("FACTUCORE.CLAVE_ACCESO.FORMATO.NUMERICO_INVALIDO");
        return resultado;
    }

    static String calcularModulo11(String valor) {
        int factor = 2, suma = 0;
        for (int i = valor.length() - 1; i >= 0; i--) {
            suma += Character.digit(valor.charAt(i), 10) * factor;
            if (++factor > 7) factor = 2;
        }
        int resultado = 11 - (suma % 11);
        return resultado == 11 ? "0" : resultado == 10 ? "1" : String.valueOf(resultado);
    }
}