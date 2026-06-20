package com.rednorte.mschatbot.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests del filtro de ms-chatbot.
 *
 * A diferencia de JwtAuthFilter en el resto de microservicios de
 * RedNorte, este filtro NUNCA bloquea la peticion (no hay ningun
 * camino que devuelva un error 401/403): el endpoint /chatbot/** es
 * publico, asi que el unico comportamiento a verificar es si autentica
 * o no segun el token, no si rechaza la peticion.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — JwtAuthFilter (autenticacion opcional)")
class JwtAuthFilterTest {

    @Mock
    private JwtUtilCompartido jwtUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filtro;

    @BeforeEach
    void setUp() {
        filtro = new JwtAuthFilter(jwtUtil);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header Authorization, continua sin autenticar y sin bloquear")
    void doFilter_sinHeaderAuthorization_continuaComoAnonimo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con header sin prefijo Bearer, continua sin autenticar")
    void doFilter_headerSinBearer_continuaComoAnonimo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic algo-irrelevante");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con token Bearer valido, autentica con el rol correspondiente")
    void doFilter_tokenValido_autenticaConRol() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-valido-123")).thenReturn(true);
        when(jwtUtil.extraerMail("token-valido-123")).thenReturn("paciente@rednorte.cl");
        when(jwtUtil.extraerRol("token-valido-123")).thenReturn("PACIENTE");

        filtro.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("paciente@rednorte.cl", auth.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Con token Bearer invalido, continua como anonimo SIN bloquear (no devuelve 401)")
    void doFilter_tokenInvalido_continuaComoAnonimoSinBloquear() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-invalido")).thenReturn(false);

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus()); // no se setea ningun codigo de error
    }

    @Test
    @DisplayName("Si jwtUtil lanza excepcion (token corrupto), continua igual sin propagar el error")
    void doFilter_tokenCorrupto_continuaSinPropagarError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-corrupto");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-corrupto")).thenThrow(new RuntimeException("JWT malformado"));

        assertDoesNotThrow(() -> filtro.doFilter(request, response, filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Si ya hay una autenticacion previa en el contexto, no la sobrescribe")
    void doFilter_contextoYaAutenticado_noLoSobrescribe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido-456");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-valido-456")).thenReturn(true);

        var authPrevia = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "ya-autenticado@rednorte.cl", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authPrevia);

        filtro.doFilter(request, response, filterChain);

        assertEquals("ya-autenticado@rednorte.cl",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(jwtUtil, never()).extraerMail(anyString());
    }
}
