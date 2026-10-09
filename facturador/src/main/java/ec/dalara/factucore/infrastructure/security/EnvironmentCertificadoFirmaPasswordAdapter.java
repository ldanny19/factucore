package ec.dalara.factucore.infrastructure.security;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.CertificadoFirmaPasswordPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnvironmentCertificadoFirmaPasswordAdapter implements CertificadoFirmaPasswordPort {

	private static final String PROPIEDAD_PASSWORD = "FACTUCORE_FIRMA_PASSWORD";

	private final Environment environment;

	@Override
	public char[] obtenerPassword() {
		String password = environment.getProperty(PROPIEDAD_PASSWORD);

		if (password == null || password.isBlank()) {
			throw new ApplicationException(MessageCodes.FIRMA_CERTIFICADO_PASSWORD_NO_CONFIGURADA);
		}

		return password.toCharArray();
	}
}
