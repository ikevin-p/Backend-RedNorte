package com.example.Backend_usuarios.security;

import com.example.Backend_usuarios.service.UserDetailsServiceImpl;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — JwtAuthFilter")
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filtro;

    @BeforeEach
    void setUp() {
        filtro = new JwtAuthFilter(jwtUtil, userDetailsService);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails userDetailsDe(String mail) {
        return new User(mail, "irrelevante", List.of());
    }

    @Test
    @DisplayName("Sin header Authorization, deja pasar sin autenticar")
    void doFilter_sinHeaderAuthorization_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("Con header sin prefijo Bearer, deja pasar sin autenticar")
    void doFilter_headerSinBearer_continuaSinAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic algo-irrelevante");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("Con token valido, autentica al usuario con sus authorities")
    void doFilter_tokenValido_autenticaUsuario() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = userDetailsDe("admin@rednorte.cl");
        when(jwtUtil.extraerMail("token-valido")).thenReturn("admin@rednorte.cl");
        when(userDetailsService.loadUserByUsername("admin@rednorte.cl")).thenReturn(userDetails);
        when(jwtUtil.esValido("token-valido", "admin@rednorte.cl")).thenReturn(true);

        filtro.doFilter(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userDetails, auth.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Con token invalido (esValido=false), no autentica pero continua la cadena")
    void doFilter_tokenInvalido_noAutenticaPeroContinua() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = userDetailsDe("admin@rednorte.cl");
        when(jwtUtil.extraerMail("token-invalido")).thenReturn("admin@rednorte.cl");
        when(userDetailsService.loadUserByUsername("admin@rednorte.cl")).thenReturn(userDetails);
        when(jwtUtil.esValido("token-invalido", "admin@rednorte.cl")).thenReturn(false);

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Si extraerMail lanza excepcion (token corrupto), no autentica ni propaga el error")
    void doFilter_tokenCorrupto_noAutenticaNiPropagaError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-corrupto");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extraerMail("token-corrupto")).thenThrow(new RuntimeException("JWT malformado"));

        assertDoesNotThrow(() -> filtro.doFilter(request, response, filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Si ya hay autenticacion previa en el contexto, no la sobrescribe")
    void doFilter_contextoYaAutenticado_noLoSobrescribe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extraerMail("token-valido")).thenReturn("admin@rednorte.cl");

        var authPrevia = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "ya-autenticado@rednorte.cl", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authPrevia);

        filtro.doFilter(request, response, filterChain);

        assertEquals("ya-autenticado@rednorte.cl",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Si el mail extraido es null, no intenta cargar el UserDetailsService")
    void doFilter_mailNulo_noConsultaUserDetailsService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-sin-mail");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extraerMail("token-sin-mail")).thenReturn(null);

        filtro.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
