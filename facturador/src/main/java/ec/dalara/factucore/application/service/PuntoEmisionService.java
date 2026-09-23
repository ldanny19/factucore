package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.PuntoEmision;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.PuntoEmisionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PuntoEmisionService extends BaseService<PuntoEmision> {

    private final PuntoEmisionRepository puntoEmisionRepository;

    @Override
    protected BaseRepository<PuntoEmision, Long> getRepository() {
        return puntoEmisionRepository;
    }

    @Override
    @Transactional
    public PuntoEmision guardar(PuntoEmision puntoEmision) {
        validar(puntoEmision);

        boolean duplicado = puntoEmision.getId() == null
                ? puntoEmisionRepository.existsByEstablecimientoIdAndCodigo(
                        puntoEmision.getEstablecimiento().getId(), puntoEmision.getCodigo())
                : puntoEmisionRepository.existsByEstablecimientoIdAndCodigoAndIdNot(
                        puntoEmision.getEstablecimiento().getId(), puntoEmision.getCodigo(), puntoEmision.getId());

        if (duplicado) {
            throw new ApplicationException(
                    MessageCodes.PUNTO_EMISION_ESTABLECIMIENTO_CODIGO_DUPLICADO,
                    puntoEmision.getEstablecimiento().getId(), puntoEmision.getCodigo());
        }

        return super.guardar(puntoEmision);
    }

    public List<PuntoEmision> listarPorEstablecimiento(Long establecimientoId) {
        if (establecimientoId == null) {
            return List.of();
        }

        return puntoEmisionRepository.findByEstablecimientoId(establecimientoId).stream()
                .filter(puntoEmision -> !EstadoRegistro.ELIMINADO.equals(puntoEmision.getEstadoRegistro()))
                .toList();
    }

    public Optional<PuntoEmision> obtenerPorEstablecimientoYCodigo(Long establecimientoId, String codigo) {
        if (establecimientoId == null || codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }

        return puntoEmisionRepository.findByEstablecimientoIdAndCodigo(establecimientoId, codigo)
                .filter(puntoEmision -> EstadoRegistro.ACTIVO.equals(puntoEmision.getEstadoRegistro()));
    }

    public boolean existePorEstablecimientoYCodigo(Long establecimientoId, String codigo) {
        if (establecimientoId == null || codigo == null || codigo.isBlank()) {
            return false;
        }

        return puntoEmisionRepository.findByEstablecimientoIdAndCodigo(establecimientoId, codigo)
                .filter(puntoEmision -> !EstadoRegistro.ELIMINADO.equals(puntoEmision.getEstadoRegistro()))
                .isPresent();
    }

    private void validar(PuntoEmision puntoEmision) {
        if (puntoEmision == null) {
            throw new ApplicationException(MessageCodes.PUNTO_EMISION_REQUERIDO);
        }

        if (puntoEmision.getEstablecimiento() == null || puntoEmision.getEstablecimiento().getId() == null) {
            throw new ApplicationException(MessageCodes.PUNTO_EMISION_ESTABLECIMIENTO_REQUERIDO);
        }

        if (puntoEmision.getCodigo() == null || puntoEmision.getCodigo().isBlank()) {
            throw new ApplicationException(MessageCodes.PUNTO_EMISION_CODIGO_REQUERIDO);
        }

        if (!puntoEmision.getCodigo().matches("\\d{3}")) {
            throw new ApplicationException(MessageCodes.PUNTO_EMISION_CODIGO_FORMATO_INVALIDO);
        }
    }
}