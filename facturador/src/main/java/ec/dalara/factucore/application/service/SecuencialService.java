package ec.dalara.factucore.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Secuencial;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.SecuencialRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecuencialService extends BaseService<Secuencial> {

    private static final long MAXIMO_SECUENCIAL = 999_999_999L;

	private final SecuencialRepository secuencialRepository;

	@Override
	protected BaseRepository<Secuencial, Long> getRepository() {
		return secuencialRepository;
	}

    @Override
    @Transactional
    public Secuencial guardar(Secuencial secuencial) {
        validar(secuencial);

        boolean duplicado = secuencial.getId() == null
                ? secuencialRepository.existsByPuntoEmisionIdAndCodigoDocumento(
                        secuencial.getPuntoEmision().getId(), secuencial.getCodigoDocumento())
                : secuencialRepository.existsByPuntoEmisionIdAndCodigoDocumentoAndIdNot(
                        secuencial.getPuntoEmision().getId(), secuencial.getCodigoDocumento(), secuencial.getId());

        if (duplicado) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_PUNTO_EMISION_DOCUMENTO_DUPLICADO,
                    secuencial.getPuntoEmision().getId(), secuencial.getCodigoDocumento());
        }

        return super.guardar(secuencial);
    }

    public Optional<Secuencial> obtenerPorPuntoEmisionYDocumento(Long puntoEmisionId, String codigoDocumento) {
        if (puntoEmisionId == null || codigoDocumento == null || codigoDocumento.isBlank()) {
            return Optional.empty();
        }

        return secuencialRepository.findByPuntoEmisionIdAndCodigoDocumento(puntoEmisionId, codigoDocumento)
				.filter(secuencial -> EstadoRegistro.ACTIVO.equals(secuencial.getEstadoRegistro()));
	}

    public boolean existePorPuntoEmisionYDocumento(Long puntoEmisionId, String codigoDocumento) {
        if (puntoEmisionId == null || codigoDocumento == null || codigoDocumento.isBlank()) {
            return false;
        }

        return secuencialRepository.findByPuntoEmisionIdAndCodigoDocumento(puntoEmisionId, codigoDocumento)
				.filter(secuencial -> !EstadoRegistro.ELIMINADO.equals(secuencial.getEstadoRegistro())).isPresent();
	}

	@Transactional
    public Long obtenerSiguienteSecuencial(Long puntoEmisionId, String codigoDocumento) {
        if (puntoEmisionId == null || codigoDocumento == null || codigoDocumento.isBlank()) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_DATOS_REQUERIDOS);
        }

        Secuencial secuencial = secuencialRepository.findByPuntoEmisionIdAndCodigoDocumentoAndEstadoRegistro(
                puntoEmisionId, codigoDocumento, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new ApplicationException(MessageCodes.SECUENCIAL_NO_ENCONTRADO,
                        puntoEmisionId, codigoDocumento));

        Long ultimo = secuencial.getUltimoSecuencial();
        if (ultimo == null || ultimo < 0) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_VALOR_INVALIDO);
        }
        if (ultimo >= MAXIMO_SECUENCIAL) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_LIMITE_ALCANZADO);
        }

        long siguiente = ultimo + 1;
        secuencial.setUltimoSecuencial(siguiente);
        secuencialRepository.save(secuencial);
        return siguiente;
    }

    private void validar(Secuencial secuencial) {
        if (secuencial == null) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_REQUERIDO);
        }
        if (secuencial.getPuntoEmision() == null || secuencial.getPuntoEmision().getId() == null) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_PUNTO_EMISION_REQUERIDO);
        }
        if (secuencial.getCodigoDocumento() == null || secuencial.getCodigoDocumento().isBlank()) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_CODIGO_DOCUMENTO_REQUERIDO);
        }
        if (!secuencial.getCodigoDocumento().matches("\\d{2}")) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_CODIGO_DOCUMENTO_FORMATO_INVALIDO);
        }
        Long ultimo = secuencial.getUltimoSecuencial();
        if (ultimo == null || ultimo < 0 || ultimo > MAXIMO_SECUENCIAL) {
            throw new ApplicationException(MessageCodes.SECUENCIAL_VALOR_INVALIDO);
        }
    }
}