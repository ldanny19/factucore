package ec.dalara.factucore.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.ConfiguracionEmpresa;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.ConfiguracionEmpresaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionEmpresaService extends BaseService<ConfiguracionEmpresa> {

    private final ConfiguracionEmpresaRepository configuracionEmpresaRepository;

    @Override
    protected BaseRepository<ConfiguracionEmpresa, Long> getRepository() {
        return configuracionEmpresaRepository;
    }

    @Override
    @Transactional
    public ConfiguracionEmpresa guardar(ConfiguracionEmpresa configuracion) {
        validar(configuracion);

        boolean duplicada = configuracion.getId() == null
                ? configuracionEmpresaRepository.existsByEmpresaIdAndClaveAndFechaVigenciaDesde(
                        configuracion.getEmpresa().getId(), configuracion.getClave(),
                        configuracion.getFechaVigenciaDesde())
                : configuracionEmpresaRepository.existsByEmpresaIdAndClaveAndFechaVigenciaDesdeAndIdNot(
                        configuracion.getEmpresa().getId(), configuracion.getClave(),
                        configuracion.getFechaVigenciaDesde(), configuracion.getId());

        if (duplicada) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_DUPLICADA,
                    configuracion.getEmpresa().getId(), configuracion.getClave(),
                    configuracion.getFechaVigenciaDesde());
        }

        return super.guardar(configuracion);
    }

    public List<ConfiguracionEmpresa> listarPorEmpresa(Long empresaId) {
        return configuracionEmpresaRepository.findByEmpresaId(empresaId).stream()
                .filter(configuracion -> !EstadoRegistro.ELIMINADO.equals(configuracion.getEstadoRegistro()))
                .toList();
    }

    public Optional<ConfiguracionEmpresa> obtenerPorEmpresaYClave(Long empresaId, String clave) {
        return configuracionEmpresaRepository.findByEmpresaIdAndClave(empresaId, clave)
                .filter(configuracion -> EstadoRegistro.ACTIVO.equals(configuracion.getEstadoRegistro()));
    }

    public boolean existePorEmpresaYClave(Long empresaId, String clave) {
        return configuracionEmpresaRepository.findByEmpresaIdAndClave(empresaId, clave)
                .filter(configuracion -> !EstadoRegistro.ELIMINADO.equals(configuracion.getEstadoRegistro()))
                .isPresent();
    }

    public Optional<ConfiguracionEmpresa> obtenerVigente(Long empresaId, String clave, LocalDateTime fecha) {
        if (empresaId == null || clave == null || clave.isBlank() || fecha == null) {
            return Optional.empty();
        }

        Optional<ConfiguracionEmpresa> configuracion = configuracionEmpresaRepository
                .findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaGreaterThanEqual(
                        empresaId, clave, EstadoRegistro.ACTIVO, fecha);

        if (configuracion.isPresent()) {
            return configuracion;
        }

        return configuracionEmpresaRepository
                .findByEmpresaIdAndClaveAndEstadoRegistroAndFechaVigenciaDesdeLessThanEqualAndFechaVigenciaHastaIsNull(
                        empresaId, clave, EstadoRegistro.ACTIVO, fecha);
    }

    private void validar(ConfiguracionEmpresa configuracion) {
        if (configuracion == null) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_REQUERIDA);
        }
        if (configuracion.getEmpresa() == null || configuracion.getEmpresa().getId() == null) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_EMPRESA_REQUERIDA);
        }
        if (configuracion.getClave() == null || configuracion.getClave().isBlank()) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_CLAVE_REQUERIDA);
        }
        if (configuracion.getTipoDato() == null || configuracion.getTipoDato().isBlank()) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_TIPO_DATO_REQUERIDO);
        }
        if (configuracion.getFechaVigenciaDesde() == null) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_FECHA_DESDE_REQUERIDA);
        }
        if (configuracion.getFechaVigenciaHasta() != null
                && configuracion.getFechaVigenciaHasta().isBefore(configuracion.getFechaVigenciaDesde())) {
            throw new ApplicationException(MessageCodes.CONFIGURACION_EMPRESA_RANGO_FECHAS_INVALIDO);
        }
    }
}