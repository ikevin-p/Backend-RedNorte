package com.rednorte.mschatbot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT de ms-chatbot: a diferencia del resto de RedNorte, este
 * filtro NUNCA bloquea una peticion por falta de token. El chatbot
 * debe responder tanto a usuarios logueados como a visitantes anonimos
 * (el system prompt mismo decide como tratarlos segun el contexto que
 * le envia ChatbotService). Si el token viene y es valido, se autentica
 * igual que en los demas microservicios; si no viene o es invalido,
 * simplemente se continua la cadena sin autenticacion.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtilCompartido jwtUtil;

    public JwtAuthFilter(JwtUtilCompartido jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtUtil.esValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String mail = jwtUtil.extraerMail(token);
                    String rol = jwtUtil.extraerRol(token);
                    var authorities = rol != null
                            ? List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                            : List.<SimpleGrantedAuthority>of();
                    var authentication = new UsernamePasswordAuthenticationToken(mail, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // Token presente pero invalido/corrupto: se trata como visitante anonimo,
                // no se rechaza la peticion (a proposito, distinto al resto del sistema).
            }
        }

        filterChain.doFilter(request, response);
    }
}
