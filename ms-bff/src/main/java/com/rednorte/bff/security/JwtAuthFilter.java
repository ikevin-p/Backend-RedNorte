package com.rednorte.bff.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT "liviano": no consulta ninguna base de datos. Confia en que
 * ms-usuarios ya valido las credenciales al emitir el token, y solo
 * verifica firma + expiracion, construyendo el Authentication directamente
 * desde los claims (mail, rol, id).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtilCompartido jwtUtil;

    public JwtAuthFilter(JwtUtilCompartido jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtUtil.esValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String mail = jwtUtil.extraerMail(token);
                String rol = jwtUtil.extraerRol(token);

                List<SimpleGrantedAuthority> authorities = rol != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                        : List.of();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(mail, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token invalido o expirado: se deja pasar sin autenticacion.
        }

        filterChain.doFilter(request, response);
    }
}
