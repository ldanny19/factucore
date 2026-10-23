package ec.dalara.factucore.notificacion.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class SeguridadNotificacionConfigTest {

	@Test
	void convierteRolesDelRealmKeycloakEnAuthoritiesDeSpring() {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
				.claim("realm_access", Map.of("roles", List.of("SUPERADMIN", "ADMIN"))).build();

		var autenticacion = new SeguridadNotificacionConfig().notificacionJwtAuthenticationConverter().convert(jwt);

		assertTrue(autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
		assertTrue(autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}
}
