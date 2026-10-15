package ec.dalara.factucore.notificacion.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.dalara.factucore.notificacion.infrastructure.persistence.NotificacionEnvio.EstadoEnvio;

public interface NotificacionEnvioRepository extends JpaRepository<NotificacionEnvio, Long> {

	Optional<NotificacionEnvio> findByIdEvento(String idEvento);

	List<NotificacionEnvio> findTop50ByEstadoEnvioOrderByFechaRecepcionAsc(EstadoEnvio estadoEnvio);

	@Modifying
	@Query("""
			update NotificacionEnvio envio
			   set envio.estadoEnvio = :estadoNuevo,
			       envio.fechaModificacion = :fechaModificacion
			 where envio.id = :id
			   and envio.estadoEnvio = :estadoActual
			""")
	int actualizarEstadoSiCoincide(@Param("id") Long id, @Param("estadoActual") EstadoEnvio estadoActual,
			@Param("estadoNuevo") EstadoEnvio estadoNuevo, @Param("fechaModificacion") Instant fechaModificacion);
}
