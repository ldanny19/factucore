package ec.dalara.factucore.seguridad;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SeguridadConfig {

	private static final String[] RECURSOS_PUBLICOS = { "/actuator/health", "/actuator/health/**", "/swagger-ui.html",
			"/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico" };

	@Bean
	SecurityFilterChain seguridadFilterChain(HttpSecurity http,
			KeycloakJwtAuthenticationConverter jwtAuthenticationConverter,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource)).csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(RECURSOS_PUBLICOS).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/comprobantes")
							.hasRole("GENERATE_COMPROBANTE")
						.requestMatchers(HttpMethod.POST, "/api/v1/comprobantes/*/reprocesar")
							.hasRole("REPROCESS_COMPROBANTE")
						.requestMatchers(HttpMethod.GET, "/api/v1/mapeos-xsd/fuentes/factucore")
							.hasRole("SUPERADMIN")
						.requestMatchers("/api/v1/**").hasAnyRole("SUPERADMIN", "ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
						.jwtAuthenticationConverter(jwtAuthenticationConverter)));

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${factucore.seguridad.cors.allowed-origins}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim)
				.filter(origin -> !origin.isEmpty()).toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("X-Transaction-Id"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${factucore.seguridad.oauth2.issuer-uri}") String issuerUri,
			@Value("${factucore.seguridad.oauth2.jwk-set-uri}") String jwkSetUri) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
		return jwtDecoder;
	}
}
