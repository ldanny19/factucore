package ec.dalara.factucore.infrastructure.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.CertificadoFirmaPasswordPort;
import ec.dalara.factucore.domain.shared.MessageCodes;

@Component
public class EnvironmentCertificadoFirmaPasswordAdapter implements CertificadoFirmaPasswordPort {

    private static final String PREFIJO = "FACTUCORE_CERTIFICADO_PASSWORD_";

    @Override
    public char[] obtenerPassword(Long empresaId) {
        if (empresaId == null) {
            throw new ApplicationException(MessageCodes.FIRMA_CERTIFICADO_EMPRESA_REQUERIDA);
        }

        String variable = PREFIJO + empresaId;
        String password = System.getenv(variable);

        if (password == null || password.isEmpty()) {
            throw new ApplicationException(
                    MessageCodes.FIRMA_CERTIFICADO_PASSWORD_NO_CONFIGURADA, empresaId);
        }

        return password.toCharArray();
    }
}
