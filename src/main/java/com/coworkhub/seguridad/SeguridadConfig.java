package com.coworkhub.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de seguridad: API sin sesiones, token JWT en cada peticion.
 * Las rutas publicas son el login, la salud, la documentacion y el frontend.
 */
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    private final JwtFiltro jwtFiltro;
    private final ManejadorErroresSeguridad manejadorErrores;

    public SeguridadConfig(JwtFiltro jwtFiltro, ManejadorErroresSeguridad manejadorErrores) {
        this.jwtFiltro = jwtFiltro;
        this.manejadorErrores = manejadorErrores;
    }

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(cabeceras -> cabeceras.frameOptions(marco -> marco.sameOrigin()))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/auth/login", "/actuator/health", "/h2-console/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc/**",
                                "/", "/index.html", "/app.js", "/styles.css", "/favicon.ico")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(manejadorErrores)
                        .accessDeniedHandler(manejadorErrores))
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration configuracion)
            throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }
}
