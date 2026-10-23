package ec.dalara.factucore.seguridad;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		Collection<GrantedAuthority> scopes = scopesConverter.convert(jwt);
		if (scopes != null) {
			authorities.addAll(scopes);
		}

		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess != null) {
			Object rolesClaim = realmAccess.get("roles");
			if (rolesClaim instanceof Collection<?> roles) {
				for (Object role : roles) {
					if (role instanceof String roleName && !roleName.isBlank()) {
						String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
						authorities.add(new SimpleGrantedAuthority(authority));
					}
				}
			}
		}
		return new JwtAuthenticationToken(jwt, authorities);
	}
}
