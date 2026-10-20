package ec.dalara.factucore.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class KeycloakJwtAuthenticationConverterTest {

	private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

	@Test
	void convierteRolesDelRealmYConservaScopes() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("scope", "openid profile")
				.claim("realm_access", Map.of("roles",
						List.of("SUPERADMIN", "ADMIN", "VENDEDOR", "GENERATE_COMPROBANTE", "REPROCESS_COMPROBANTE")))
				.build();

		JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

		assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.contains("ROLE_SUPERADMIN", "ROLE_ADMIN", "ROLE_VENDEDOR", "ROLE_GENERATE_COMPROBANTE",
						"ROLE_REPROCESS_COMPROBANTE", "SCOPE_openid", "SCOPE_profile");
	}

	@Test
	void noConcedeRolesDeClientesQueNoSeanRolesDelRealm() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("resource_access", Map.of("otro-cliente",
						Map.of("roles", List.of("GENERATE_COMPROBANTE", "REPROCESS_COMPROBANTE"))))
				.build();

		JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

		assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.doesNotContain("ROLE_GENERATE_COMPROBANTE", "ROLE_REPROCESS_COMPROBANTE");
	}
}
