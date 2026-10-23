package ec.dalara.factucore.notificacion.infrastructure.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SeguridadNotificacionConfig {

	@Bean
	SecurityFilterChain notificacionSecurityFilterChain(HttpSecurity http,
			CorsConfigurationSource notificacionCorsConfigurationSource,
			Converter<Jwt, AbstractAuthenticationToken> notificacionJwtAuthenticationConverter) throws Exception {
		http.cors(cors -> cors.configurationSource(notificacionCorsConfigurationSource))
				.csrf(csrf -> csrf.disable()).formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/comprobantes/*/envios")
						.hasAnyRole("SUPERADMIN", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/comprobantes/*/reenvios")
						.hasAnyRole("SUPERADMIN", "ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(
						jwt -> jwt.jwtAuthenticationConverter(notificacionJwtAuthenticationConverter)));
		return http.build();
	}

	@Bean
	CorsConfigurationSource notificacionCorsConfigurationSource(
			@Value("${factucore.seguridad.cors.allowed-origins}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim)
				.filter(origin -> !origin.isEmpty()).toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	Converter<Jwt, AbstractAuthenticationToken> notificacionJwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
		return jwt -> {
			var authorities = new LinkedHashSet<GrantedAuthority>();
			var scopes = scopesConverter.convert(jwt);
			if (scopes != null) {
				authorities.addAll(scopes);
			}
			Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
			if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
				roles.stream().filter(String.class::isInstance).map(String.class::cast)
						.filter(role -> !role.isBlank())
						.map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
						.forEach(authorities::add);
			}
			return new JwtAuthenticationToken(jwt, authorities);
		};
	}

	@Bean
	JwtDecoder notificacionJwtDecoder(@Value("${factucore.seguridad.oauth2.issuer-uri}") String issuerUri,
			@Value("${factucore.seguridad.oauth2.jwk-set-uri}") String jwkSetUri) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
		return jwtDecoder;
	}
}
