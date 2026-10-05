package ec.dalara.factucore.infrastructure.persistence.audit;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaHibernateInterceptor implements Interceptor, HibernatePropertiesCustomizer {

	private static final String ESTADO_REGISTRO = "estadoRegistro";
	private static final String USUARIO_CREACION = "usuarioCreacion";
	private static final String USUARIO_MODIFICACION = "usuarioModificacion";
	private static final String FECHA_CREACION = "fechaCreacion";
	private static final String FECHA_MODIFICACION = "fechaModificacion";
	private static final String ESTADO_ACTIVO = "A";

	private final Clock factuCoreClock;

	public AuditoriaHibernateInterceptor(Clock factuCoreClock) {
		this.factuCoreClock = factuCoreClock;
	}

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put("hibernate.session_factory.interceptor", this);
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {

		LocalDateTime ahora = LocalDateTime.now(factuCoreClock);
		String usuario = usuarioAutenticado();

		boolean auditado = false;

		if (setProperty(state, propertyNames, ESTADO_REGISTRO, ESTADO_ACTIVO)) {
			auditado = true;
		}
		if (setProperty(state, propertyNames, USUARIO_CREACION, usuario)) {
			auditado = true;
		}
		if (setProperty(state, propertyNames, FECHA_CREACION, ahora)) {
			auditado = true;
		}

		return auditado;
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {

		boolean auditado = false;

		if (hasProperty(propertyNames, USUARIO_MODIFICACION) || hasProperty(propertyNames, FECHA_MODIFICACION)) {

			String usuario = usuarioAutenticado();
			LocalDateTime ahora = LocalDateTime.now(factuCoreClock);

			auditado |= setProperty(currentState, propertyNames, USUARIO_MODIFICACION, usuario);
			auditado |= setProperty(currentState, propertyNames, FECHA_MODIFICACION, ahora);
		}

		return auditado;
	}

	private String usuarioAutenticado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("No existe un usuario autenticado para realizar la auditoria.");
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof Jwt jwt) {
			String preferredUsername = jwt.getClaimAsString("preferred_username");
			if (preferredUsername != null && !preferredUsername.isBlank()) {
				return preferredUsername;
			}
			return jwt.getSubject();
		}

		String name = authentication.getName();
		if (name == null || name.isBlank()) {
			throw new IllegalStateException("No existe un usuario autenticado para realizar la auditoria.");
		}

		return name;
	}

	private boolean hasProperty(String[] propertyNames, String propertyName) {
		for (String current : propertyNames) {
			if (propertyName.equals(current)) {
				return true;
			}
		}
		return false;
	}

	private boolean setProperty(Object[] state, String[] propertyNames, String propertyName, Object value) {

		for (int i = 0; i < propertyNames.length; i++) {
			if (propertyName.equals(propertyNames[i])) {
				state[i] = value;
				return true;
			}
		}

		return false;
	}
}
