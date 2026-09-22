package ec.dalara.factucore.infrastructure.persistence.adapter;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.application.port.out.SecuencialPort;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import ec.dalara.factucore.infrastructure.persistence.repository.SecuencialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSecuencialAdapter implements SecuencialPort {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final SecuencialRepository secuencialRepository;

    @Override
    @Transactional
    public String obtenerSiguiente(
            Long puntoEmisionId,
            String codigoDocumento
    ) {
        if (puntoEmisionId == null) {
            throw new ApplicationException(
                    MessageCodes.WORKFLOW_COMPROBANTE_REQUERIDO
            );
        }

        if (codigoDocumento == null || codigoDocumento.isBlank()) {
            throw new ApplicationException(
                    MessageCodes.COMPROBANTE_CODIGO_DOCUMENTO_REQUERIDO
            );
        }

        Secuencial secuencial =
                secuencialRepository
                        .findByPuntoEmisionIdAndCodigoDocumentoAndEstadoRegistro(
                                puntoEmisionId,
                                codigoDocumento,
                                ESTADO_ACTIVO
                        )
                        .orElseThrow(() ->
                                new ApplicationException(
                                        MessageCodes.SECUENCIAL_NO_ENCONTRADO
                                )
                        );

        long siguiente =
                secuencial.getUltimoSecuencial() + 1;

        if (siguiente > 999_999_999L) {
            throw new ApplicationException(
                    MessageCodes.SECUENCIAL_LIMITE_ALCANZADO
            );
        }

        secuencial.setUltimoSecuencial(siguiente);

        return String.format("%09d", siguiente);
    }
}