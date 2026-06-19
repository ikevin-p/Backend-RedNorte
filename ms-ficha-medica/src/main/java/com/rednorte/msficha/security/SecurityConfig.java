package com.rednorte.msficha.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad de ms-ficha-medica: exige JWT valido en todos los endpoints,
 * salvo Swagger, actuator y preflight CORS. Las fichas medicas son
 * informacion clinica sensible: no deben quedar accesibles sin token.
 *
 * El CORS lo maneja exclusivamente el API Gateway (globalcors); agregarlo
 * tambien aqui duplicaria el header Access-Control-Allow-Origin.
 *
 * NOTA: esta clase fue borrada por error junto con la de ms-consultas y
 * Backend_reasignacion durante una limpieza de archivos del frontend
 * (un Remove-Item con llaves mal interpretadas por PowerShell tambien
 * afecto este archivo). Sin SecurityFilterChain propio, Spring Boot
 * aplica su configuracion default de Spring Security, que redirige a
 * una pagina /login inexistente en vez de devolver 401 — esto causaba
 * los errores "ERR_CONNECTION_TIMED_OUT a 172.x.x.x:8084/login" vistos
 * en el frontend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
