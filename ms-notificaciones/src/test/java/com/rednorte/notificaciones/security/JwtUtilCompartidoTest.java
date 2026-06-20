package com.rednorte.notificaciones.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitarios — JwtUtilCompartido")
class JwtUtilCompartidoTest {

    private static final String SECRET = "rednorte-clinica-digital-clave-secreta-jwt-2026-super-segura";

    private JwtUtilCompartido jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtilCompartido();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generarToken(String mail, String rol, String id, long minutosValidez) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + minutosValidez * 60_000);
        return Jwts.builder()
                .subject(mail)
                .claim("rol", rol)
                .claim("id", id)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getKey())
                .compact();
    }

    @Test
    @DisplayName("Extrae el mail (subject) correctamente de un token valido")
    void extraerMail_tokenValido_retornaMail() {
        String token = generarToken("paciente@rednorte.cl", "PACIENTE", "USR010", 30);
        assertEquals("paciente@rednorte.cl", jwtUtil.extraerMail(token));
    }

    @Test
    @DisplayName("Extrae el rol correctamente de un token valido")
    void extraerRol_tokenValido_retornaRol() {
        String token = generarToken("admin@rednorte.cl", "ADMIN", "USR001", 30);
        assertEquals("ADMIN", jwtUtil.extraerRol(token));
    }

    @Test
    @DisplayName("Extrae el id correctamente de un token valido")
    void extraerId_tokenValido_retornaId() {
        String token = generarToken("dr.vega@rednorte.cl", "DOCTOR", "USR002", 30);
        assertEquals("USR002", jwtUtil.extraerId(token));
    }

    @Test
    @DisplayName("Un token recien emitido y no expirado es valido")
    void esValido_tokenNoExpirado_retornaTrue() {
        String token = generarToken("paciente@rednorte.cl", "PACIENTE", "USR010", 30);
        assertTrue(jwtUtil.esValido(token));
    }

    @Test
    @DisplayName("Un token con fecha de expiracion en el pasado no es valido")
    void esValido_tokenExpirado_retornaFalse() {
        String token = generarToken("paciente@rednorte.cl", "PACIENTE", "USR010", -10);
        assertFalse(jwtUtil.esValido(token));
    }

    @Test
    @DisplayName("esValido detecta como invalido un token ya vencido (via estaExpirado)")
    void estaExpirado_tokenVencido_haceQueEsValidoSeaFalse() {
        String token = generarToken("paciente@rednorte.cl", "PACIENTE", "USR010", -5);
        assertFalse(jwtUtil.esValido(token));
    }

    @Test
    @DisplayName("estaExpirado retorna false para un token todavia vigente")
    void estaExpirado_tokenVigente_retornaFalse() {
        String token = generarToken("paciente@rednorte.cl", "PACIENTE", "USR010", 30);
        assertFalse(jwtUtil.estaExpirado(token));
    }

    @Test
    @DisplayName("esValido retorna false ante un token mal formado (no es JWT)")
    void esValido_tokenMalFormado_retornaFalse() {
        assertFalse(jwtUtil.esValido("esto-no-es-un-jwt-real"));
    }

    @Test
    @DisplayName("esValido retorna false ante un token firmado con otra clave")
    void esValido_tokenFirmadoConOtraClave_retornaFalse() {
        SecretKey otraClave = Keys.hmacShaKeyFor(
                "otra-clave-secreta-completamente-distinta-de-32-bytes".getBytes(StandardCharsets.UTF_8));
        String tokenAjeno = Jwts.builder()
                .subject("intruso@externo.cl")
                .claim("rol", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otraClave)
                .compact();
        assertFalse(jwtUtil.esValido(tokenAjeno));
    }

    @Test
    @DisplayName("extraerClaims retorna los claims completos del payload")
    void extraerClaims_tokenValido_retornaPayloadCompleto() {
        String token = generarToken("dra.rojas@rednorte.cl", "DOCTOR", "USR003", 30);
        var claims = jwtUtil.extraerClaims(token);
        assertEquals("dra.rojas@rednorte.cl", claims.getSubject());
        assertEquals("DOCTOR", claims.get("rol", String.class));
        assertEquals("USR003", claims.get("id", String.class));
    }
}
