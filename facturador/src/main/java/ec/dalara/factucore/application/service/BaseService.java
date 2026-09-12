package ec.dalara.factucore.application.service;

import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.EstadoRegistroEntity;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public abstract class BaseService<T extends EstadoRegistroEntity> {

    protected abstract BaseRepository<T, Long> getRepository();

    public Optional<T> obtenerPorId(Long id) {
        return getRepository().findByIdAndEstadoRegistro(
                id,
                EstadoRegistro.ACTIVO
        );
    }

    public List<T> listar() {
        return getRepository().findByEstadoRegistroNot(
                EstadoRegistro.ELIMINADO
        );
    }

    @Transactional
    public T guardar(T entidad) {

        if (entidad.getEstadoRegistro() == null) {
            entidad.setEstadoRegistro(EstadoRegistro.ACTIVO);
        }

        return getRepository().save(entidad);
    }

    @Transactional
    public T inactivar(Long id) {

        T entidad = getRepository()
                .findById(id)
                .orElseThrow();

        entidad.setEstadoRegistro(EstadoRegistro.INACTIVO);

        return getRepository().save(entidad);
    }

    @Transactional
    public T eliminar(Long id) {

        T entidad = getRepository()
                .findById(id)
                .orElseThrow();

        entidad.setEstadoRegistro(EstadoRegistro.ELIMINADO);

        return getRepository().save(entidad);
    }
}