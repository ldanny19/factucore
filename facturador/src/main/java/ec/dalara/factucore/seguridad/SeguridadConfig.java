package ec.dalara.factucore.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del API FactuCore.
 *
 * <p>La seguridad se habilita mediante FACTUCORE_SEGURIDAD_HABILITADA.
 * Cuando está habilitada, el API valida Bearer JWT emitidos por Keycloak.
 * Cuando está deshabilitada, se permite el acceso para facilitar el desarrollo
 * local sin eliminar la implementación real de seguridad.</p>
 */
@Configuration
public class SeguridadConfig {

    private static final String[] RECURSOS_PUBLICOS = {
            "/actuator/health",
            "/actuator/health/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico"
    };

    @Bean
    SecurityFilterChain seguridadFilterChain(
            HttpSecurity http,
            @Value("${factucore.seguridad.habilitada:false}") boolean seguridadHabilitada) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(RECURSOS_PUBLICOS).permitAll();

                    if (seguridadHabilitada) {
                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                });

        if (seguridadHabilitada) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${factucore.seguridad.habilitada:false}") boolean seguridadHabilitada,
            @Value("${factucore.seguridad.oauth2.issuer-uri}") String issuerUri) {

        if (!seguridadHabilitada) {
            return token -> {
                throw new IllegalStateException("JWT decoder no disponible con seguridad deshabilitada");
            };
        }

        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
