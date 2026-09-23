package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.CertificadoFirma;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CertificadoFirmaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificadoFirmaService extends BaseService<CertificadoFirma> {

	private final CertificadoFirmaRepository certificadoFirmaRepository;

	@Override
	protected BaseRepository<CertificadoFirma, Long> getRepository() {
		return certificadoFirmaRepository;
	}

    @Override
    @Transactional
    public CertificadoFirma guardar(CertificadoFirma certificado) {
        validar(certificado);
        return super.guardar(certificado);
    }

    public List<CertificadoFirma> listarPorEmpresa(Long empresaId) {
		return certificadoFirmaRepository.findByEmpresaId(empresaId).stream()
				.filter(certificado -> !EstadoRegistro.ELIMINADO.equals(certificado.getEstadoRegistro())).toList();
	}

    public Optional<CertificadoFirma> obtenerVigente(Long empresaId, LocalDateTime fecha) {
        if (empresaId == null || fecha == null) {
            return Optional.empty();
        }
		Optional<CertificadoFirma> certificado = certificadoFirmaRepository
				.findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(empresaId,
						EstadoRegistro.ACTIVO, fecha);

		if (certificado.isPresent()) {
			return certificado;
		}

		return certificadoFirmaRepository.findByEmpresaIdAndEstadoRegistroAndFechaInicioLessThanEqualAndFechaFinIsNull(
				empresaId, EstadoRegistro.ACTIVO, fecha);
    }

    private void validar(CertificadoFirma certificado) {
        if (certificado == null) throw new ApplicationException(MessageCodes.CERTIFICADO_FIRMA_REQUERIDO);
        if (certificado.getEmpresa() == null || certificado.getEmpresa().getId() == null)
            throw new ApplicationException(MessageCodes.CERTIFICADO_FIRMA_EMPRESA_REQUERIDA);
        if (certificado.getRutaCertificado() == null || certificado.getRutaCertificado().isBlank())
            throw new ApplicationException(MessageCodes.CERTIFICADO_FIRMA_RUTA_REQUERIDA);
        if (certificado.getFechaInicio() == null)
            throw new ApplicationException(MessageCodes.CERTIFICADO_FIRMA_FECHA_INICIO_REQUERIDA);
        if (certificado.getFechaFin() != null && certificado.getFechaFin().isBefore(certificado.getFechaInicio()))
            throw new ApplicationException(MessageCodes.CERTIFICADO_FIRMA_RANGO_FECHAS_INVALIDO);
    }
}