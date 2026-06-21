package com.example.Backend_usuarios.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de JwtUtil. A diferencia del resto de microservicios
 * de RedNorte (que solo VALIDAN tokens generados por este servicio),
 * JwtUtil tambien GENERA el token original, asi que se prueba el
 * ciclo completo: generar -> extraer -> validar, sin necesidad de
 * construir tokens externos a mano con jjwt.
 */
@DisplayName("Tests unitarios — JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    @DisplayName("generarToken produce un JWT con 3 partes separadas por punto")
    void generarToken_produceJwtValido() {
        String token = jwtUtil.generarToken("admin@rednorte.cl", "ADMIN", "USR001");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("extraerMail recupera el mail (subject) del token generado")
    void extraerMail_tokenPropio_retornaMailCorrecto() {
        String token = jwtUtil.generarToken("paciente@rednorte.cl", "PACIENTE", "USR010");
        assertEquals("paciente@rednorte.cl", jwtUtil.extraerMail(token));
    }

    @Test
    @DisplayName("extraerRol recupera el rol del token generado")
    void extraerRol_tokenPropio_retornaRolCorrecto() {
        String token = jwtUtil.generarToken("dr.vega@rednorte.cl", "DOCTOR", "USR002");
        assertEquals("DOCTOR", jwtUtil.extraerRol(token));
    }

    @Test
    @DisplayName("extraerClaim con resolver personalizado recupera el id de usuario")
    void extraerClaim_resolverPersonalizado_recuperaIdUsuario() {
        String token = jwtUtil.generarToken("dra.rojas@rednorte.cl", "DOCTOR", "USR003");
        String id = jwtUtil.extraerClaim(token, c -> c.get("id", String.class));
        assertEquals("USR003", id);
    }

    @Test
    @DisplayName("esValido retorna true cuando el mail coincide y el token no expiro")
    void esValido_mailCoincideYNoExpirado_retornaTrue() {
        String token = jwtUtil.generarToken("admin@rednorte.cl", "ADMIN", "USR001");
        assertTrue(jwtUtil.esValido(token, "admin@rednorte.cl"));
    }

    @Test
    @DisplayName("esValido retorna false cuando el mail NO coincide con el del token")
    void esValido_mailNoCoincide_retornaFalse() {
        String token = jwtUtil.generarToken("admin@rednorte.cl", "ADMIN", "USR001");
        assertFalse(jwtUtil.esValido(token, "otro@rednorte.cl"));
    }

    @Test
    @DisplayName("esValido retorna false ante un token mal formado")
    void esValido_tokenMalFormado_retornaFalse() {
        assertFalse(jwtUtil.esValido("esto-no-es-un-jwt", "admin@rednorte.cl"));
    }

    @Test
    @DisplayName("estaExpirado retorna false para un token recien generado")
    void estaExpirado_tokenRecienGenerado_retornaFalse() {
        String token = jwtUtil.generarToken("admin@rednorte.cl", "ADMIN", "USR001");
        assertFalse(jwtUtil.estaExpirado(token));
    }
}
