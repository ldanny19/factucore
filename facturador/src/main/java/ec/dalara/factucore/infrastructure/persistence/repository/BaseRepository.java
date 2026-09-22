package ec.dalara.factucore.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

	Optional<T> findByIdAndEstadoRegistro(ID id, String estadoRegistro);

	List<T> findByEstadoRegistro(String estadoRegistro);

	List<T> findByEstadoRegistroNot(String estadoRegistro);
}