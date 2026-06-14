package com.example.Backend_usuarios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clave secreta (minimo 32 caracteres para HS256). En produccion iria en variable de entorno.
    private static final String SECRET = "rednorte-clinica-digital-clave-secreta-jwt-2026-super-segura";
    private static final long EXPIRACION_MS = 1000 * 60 * 60 * 8; // 8 horas

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String mail, String rolTag, String usuarioId) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + EXPIRACION_MS);
        return Jwts.builder()
                .subject(mail)
                .claim("rol", rolTag)
                .claim("id", usuarioId)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(getKey())
                .compact();
    }

    public String extraerMail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerRol(String token) {
        return extraerClaim(token, c -> c.get("rol", String.class));
    }

    public <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    public boolean esValido(String token, String mail) {
        try {
            String tokenMail = extraerMail(token);
            return tokenMail.equals(mail) && !estaExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }
}
