package com.rednorte.msestablecimientos.security;

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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — JwtAuthFilter")
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
    @DisplayName("Sin header Authorization, deja pasar la peticion sin autenticar")
    void doFilter_sinHeaderAuthorization_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con header que no empieza con 'Bearer ', deja pasar sin autenticar")
    void doFilter_headerSinPrefijoBearer_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic algo-irrelevante");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con token Bearer valido, autentica al usuario con su rol")
    void doFilter_tokenValido_autenticaConRolCorrecto() throws Exception {
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
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Con token Bearer invalido (esValido=false), no autentica pero continua la cadena")
    void doFilter_tokenInvalido_noAutenticaPeroContinua() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-invalido")).thenReturn(false);

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extraerMail(anyString());
    }

    @Test
    @DisplayName("Si jwtUtil lanza excepcion (token corrupto), el filtro no propaga el error")
    void doFilter_jwtUtilLanzaExcepcion_continuaSinAutenticarNiFallar() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-corrupto");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-corrupto")).thenThrow(new RuntimeException("JWT malformado"));

        assertDoesNotThrow(() -> filtro.doFilter(request, response, filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Si ya existe una autenticacion previa en el contexto, no la sobrescribe")
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
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Con rol null en el token, autentica sin authorities (lista vacia)")
    void doFilter_tokenSinRol_autenticaSinAuthorities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-sin-rol");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.esValido("token-sin-rol")).thenReturn(true);
        when(jwtUtil.extraerMail("token-sin-rol")).thenReturn("sinrol@rednorte.cl");
        when(jwtUtil.extraerRol("token-sin-rol")).thenReturn(null);

        filtro.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
    }
}
