package com.rednorte.bff.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Utilidad de validacion de JWT compartida entre microservicios.
 *
 * Este microservicio NO genera tokens (eso lo hace ms-usuarios al hacer login).
 * Solo valida la firma y expiracion, y extrae los claims (mail, rol, id)
 * para construir el contexto de seguridad localmente, sin consultar
 * la base de datos de usuarios (que pertenece a otro microservicio).
 *
 * La clave secreta debe coincidir exactamente con la usada en
 * Backend-usuarios/src/main/java/.../security/JwtUtil.java
 */
@Component
public class JwtUtilCompartido {

    private static final String SECRET = "rednorte-clinica-digital-clave-secreta-jwt-2026-super-segura";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerMail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerRol(String token) {
        return extraerClaim(token, c -> c.get("rol", String.class));
    }

    public String extraerId(String token) {
        return extraerClaim(token, c -> c.get("id", String.class));
    }

    public <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerClaims(token));
    }

    public boolean esValido(String token) {
        try {
            return !estaExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }
}
