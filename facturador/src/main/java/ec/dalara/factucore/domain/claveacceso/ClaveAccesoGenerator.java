package ec.dalara.factucore.domain.claveacceso;

import java.time.format.DateTimeFormatter;

import ec.dalara.factucore.domain.shared.DomainException;

public final class ClaveAccesoGenerator {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");

	private ClaveAccesoGenerator() {
	}

	public static ClaveAccesoModel generar(ClaveAccesoDatos datos) {
		validar(datos);

		String fecha = datos.fechaEmision().format(FORMATO_FECHA);
		String codigoDocumento = validarNumerico(datos.codigoDocumento(), 2);
		String ruc = validarNumerico(datos.ruc(), 13);
		String ambiente = validarNumerico(datos.ambiente(), 1);
		String establecimiento = validarNumerico(datos.establecimiento(), 3);
		String puntoEmision = validarNumerico(datos.puntoEmision(), 3);
		String secuencial = validarNumerico(datos.secuencial(), 9);
		String codigoNumerico = validarNumerico(datos.codigoNumerico(), 8);
		String tipoEmision = validarNumerico(datos.tipoEmision(), 1);

		String base = fecha + codigoDocumento + ruc + ambiente + establecimiento + puntoEmision + secuencial
				+ codigoNumerico + tipoEmision;

		String digitoVerificador = calcularModulo11(base);
		String clave = base + digitoVerificador;

		return new ClaveAccesoModel(clave, fecha, codigoDocumento, ruc, ambiente, establecimiento + puntoEmision,
				secuencial, codigoNumerico, tipoEmision, digitoVerificador);
	}

	private static void validar(ClaveAccesoDatos datos) {
		if (datos == null) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.DATOS.REQUERIDOS");
		}

		if (datos.fechaEmision() == null) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.FECHA.REQUERIDA");
		}
	}

	private static String validarNumerico(String valor, int longitud) {
		if (valor == null || valor.isBlank()) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.VALOR.REQUERIDO");
		}

		String resultado = valor.trim();

		if (!resultado.matches("\\d{" + longitud + "}")) {
			throw new DomainException("FACTUCORE.CLAVE_ACCESO.FORMATO.NUMERICO_INVALIDO");
		}

		return resultado;
	}

	private static String calcularModulo11(String valor) {
		int factor = 2;
		int suma = 0;

		for (int i = valor.length() - 1; i >= 0; i--) {
			suma += Character.digit(valor.charAt(i), 10) * factor;
			factor++;

			if (factor > 7) {
				factor = 2;
			}
		}

		int residuo = suma % 11;

		if (residuo == 0) {
			return "0";
		}

		int resultado = 11 - residuo;

		if (resultado == 10) {
			return "1";
		}

		return String.valueOf(resultado);
	}
}
