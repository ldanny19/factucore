package ec.dalara.factucore.domain.claveacceso;

public final class ClaveAccesoValidator {

	private ClaveAccesoValidator() {
	}

	public static boolean esValida(String clave) {
		if (clave == null || clave.length() != 49) {
			return false;
		}

		if (!clave.matches("\\d{49}")) {
			return false;
		}

		String base = clave.substring(0, 48);

		String esperado = calcularModulo11(base);

		return esperado.equals(clave.substring(48));
	}

	private static String calcularModulo11(String valor) {
		int factor = 2;
		int suma = 0;

		for (int i = valor.length() - 1; i >= 0; i--) {

			int digito = Character.digit(valor.charAt(i), 10);

			suma += digito * factor;

			factor++;

			if (factor > 7) {
				factor = 2;
			}
		}

		int resultado = 11 - (suma % 11);

		if (resultado == 11) {
			return "0";
		}

		if (resultado == 10) {
			return "1";
		}

		return String.valueOf(resultado);
	}
}