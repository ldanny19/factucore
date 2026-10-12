package ec.dalara.factucore.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración obligatoria de seguridad del API FactuCore.
 *
 * <p>
 * Facturador actúa exclusivamente como OAuth2 Resource Server. La autenticación
 * se realiza en Keycloak y el frontend envía el Bearer JWT. Facturador no
 * implementa login, formulario, HTTP Basic ni gestión de credenciales de
 * usuario.
 * </p>
 */
@Configuration
public class SeguridadConfig {

	private static final String[] RECURSOS_PUBLICOS = { "/actuator/health", "/actuator/health/**", "/swagger-ui.html",
			"/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico" };

	@Bean
	SecurityFilterChain seguridadFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
				.authorizeHttpRequests(authorize -> authorize.requestMatchers(RECURSOS_PUBLICOS).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${factucore.seguridad.oauth2.issuer-uri}") String issuerUri,
			@Value("${factucore.seguridad.oauth2.jwk-set-uri}") String jwkSetUri) {

		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));

		return jwtDecoder;
	}
}
