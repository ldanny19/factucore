package ec.dalara.factucore.domain.claveacceso;

import java.security.SecureRandom;

public final class ClaveAccesoCodigoNumericoGenerator {

	private static final int LONGITUD = 8;

	private static final SecureRandom RANDOM = new SecureRandom();

	private ClaveAccesoCodigoNumericoGenerator() {
	}

	public static String generar() {

		int limite = 100_000_000;

		int valor = RANDOM.nextInt(limite);

		return String.format("%08d", valor);
	}
}