package com.rednorte.mschatbot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad de ms-chatbot.
 *
 * IMPORTANTE - distinto al resto de microservicios de RedNorte:
 * /chatbot/** es publico a proposito (permitAll), porque el bot debe
 * responder tanto a usuarios logueados como a visitantes anonimos.
 * JwtAuthFilter SI valida el JWT cuando viene (para autenticar al
 * usuario si corresponde), pero nunca bloquea la peticion por su
 * ausencia.
 *
 * TRADE-OFF DE SEGURIDAD CONSCIENTE: como el endpoint es publico, el
 * usuarioId que identifica al paciente viaja en el body de la peticion
 * (MensajeRequestDTO), no se deriva exclusivamente del JWT como en el
 * resto del sistema. El impacto esta acotado: como mucho alguien podria
 * hacer que SaludBot cree una consulta a nombre de otro usuarioId
 * existente (no puede leer datos privados ni autenticarse como ese
 * usuario en ningun otro microservicio).
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
                .requestMatchers("/chatbot/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
