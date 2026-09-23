package ec.dalara.factucore.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;

@Transactional(readOnly = true)
public abstract class BaseService<T extends EstadoRegistroEntity> {

    protected abstract BaseRepository<T, Long> getRepository();

    public Optional<T> obtenerPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return getRepository().findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO);
    }

    public List<T> listar() {
        return getRepository().findByEstadoRegistroNot(EstadoRegistro.ELIMINADO);
    }

    @Transactional
    public T guardar(T entidad) {
        if (entidad == null) {
            throw new ApplicationException(MessageCodes.REGISTRO_REQUERIDO);
        }

        if (entidad.getEstadoRegistro() == null) {
            entidad.setEstadoRegistro(EstadoRegistro.ACTIVO);
        }

        if (EstadoRegistro.ELIMINADO.equals(entidad.getEstadoRegistro())) {
            throw new ApplicationException(MessageCodes.REGISTRO_ELIMINADO_NO_MODIFICABLE);
        }

        return getRepository().save(entidad);
    }

    @Transactional
    public T inactivar(Long id) {
        T entidad = obtenerAdministrable(id);
        if (!EstadoRegistro.ACTIVO.equals(entidad.getEstadoRegistro())) {
            throw new ApplicationException(MessageCodes.REGISTRO_ESTADO_INVALIDO, EstadoRegistro.ACTIVO);
        }

        entidad.setEstadoRegistro(EstadoRegistro.INACTIVO);
        return getRepository().save(entidad);
    }

    @Transactional
    public T reactivar(Long id) {
        if (id == null) {
            throw new ApplicationException(MessageCodes.REGISTRO_ID_REQUERIDO);
        }

        T entidad = getRepository().findByIdAndEstadoRegistro(id, EstadoRegistro.INACTIVO)
                .orElseThrow(() -> new ApplicationException(MessageCodes.REGISTRO_NO_ENCONTRADO, id));

        entidad.setEstadoRegistro(EstadoRegistro.ACTIVO);
        return getRepository().save(entidad);
    }

    @Transactional
    public T eliminar(Long id) {
        T entidad = obtenerAdministrable(id);
        if (!EstadoRegistro.ACTIVO.equals(entidad.getEstadoRegistro())
                && !EstadoRegistro.INACTIVO.equals(entidad.getEstadoRegistro())) {
            throw new ApplicationException(MessageCodes.REGISTRO_ESTADO_INVALIDO,
                    EstadoRegistro.ACTIVO + "/" + EstadoRegistro.INACTIVO);
        }

        entidad.setEstadoRegistro(EstadoRegistro.ELIMINADO);
        return getRepository().save(entidad);
    }

    private T obtenerAdministrable(Long id) {
        if (id == null) {
            throw new ApplicationException(MessageCodes.REGISTRO_ID_REQUERIDO);
        }

        return getRepository().findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .or(() -> getRepository().findByIdAndEstadoRegistro(id, EstadoRegistro.INACTIVO))
                .orElseThrow(() -> new ApplicationException(MessageCodes.REGISTRO_NO_ENCONTRADO, id));
    }
}
