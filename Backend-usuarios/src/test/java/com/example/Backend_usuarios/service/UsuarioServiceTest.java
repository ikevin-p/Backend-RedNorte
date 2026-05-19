package com.example.Backend_usuarios.service;

import com.example.Backend_usuarios.model.Persona;
import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.repository.RolRepository;
import com.example.Backend_usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioMock;
    private Rol rolMock;

    @BeforeEach
    void setUp() {
        rolMock = new Rol();
        rolMock.setId("ROL001");
        rolMock.setTag("ADMIN");

        Persona personaMock = new Persona();
        personaMock.setId("PER001");
        personaMock.setRut("12345678-9");

        usuarioMock = new Usuario();
        usuarioMock.setId("USR001");
        usuarioMock.setMail("admin@rednorte.cl");
        usuarioMock.setPass("$2a$10$hashFicticio");
        usuarioMock.setEstado("ACTIVO");
        usuarioMock.setRol(rolMock);
        usuarioMock.setPersona(personaMock);
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Login exitoso con credenciales correctas")
    void login_credencialesCorrectas_retornaUsuario() {
        when(usuarioRepository.findByMail("admin@rednorte.cl"))
            .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("admin123", "$2a$10$hashFicticio"))
            .thenReturn(true);

        Usuario resultado = usuarioService.login("admin@rednorte.cl", "admin123");

        assertNotNull(resultado);
        assertEquals("admin@rednorte.cl", resultado.getMail());
        assertEquals("USR001", resultado.getId());
        verify(usuarioRepository, times(1)).findByMail("admin@rednorte.cl");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login falla con contraseña incorrecta")
    void login_contrasenaIncorrecta_retornaNull() {
        when(usuarioRepository.findByMail("admin@rednorte.cl"))
            .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashFicticio"))
            .thenReturn(false);

        Usuario resultado = usuarioService.login("admin@rednorte.cl", "wrongpass");

        assertNull(resultado);
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login falla cuando el mail no existe")
    void login_mailNoExiste_retornaNull() {
        when(usuarioRepository.findByMail("noexiste@rednorte.cl"))
            .thenReturn(Optional.empty());

        Usuario resultado = usuarioService.login("noexiste@rednorte.cl", "cualquier");

        assertNull(resultado);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ─── LISTAR ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar usuarios retorna lista completa")
    void usuarioListar_retornaListaCompleta() {
        Usuario u2 = new Usuario();
        u2.setId("USR002");
        u2.setMail("dr.vega@rednorte.cl");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioMock, u2));

        List<Usuario> resultado = usuarioService.usuarioListar();

        assertEquals(2, resultado.size());
        assertEquals("USR001", resultado.get(0).getId());
        assertEquals("USR002", resultado.get(1).getId());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Listar usuarios retorna lista vacía cuando no hay usuarios")
    void usuarioListar_sinUsuarios_retornaListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<Usuario> resultado = usuarioService.usuarioListar();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ─── ALMACENAR ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Almacenar usuario hashea la contraseña antes de guardar")
    void usuarioAlmacenar_contrasenaEsHasheadaAntesDeGuardar() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setId("USR099");
        nuevoUsuario.setMail("nuevo@rednorte.cl");
        nuevoUsuario.setPass("plaintext123");

        Persona persona = new Persona();
        persona.setId("PER099");
        nuevoUsuario.setPersona(persona);

        when(passwordEncoder.encode("plaintext123")).thenReturn("$2a$10$nuevoHash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevoUsuario);

        usuarioService.usuarioAlmacenar(nuevoUsuario);

        verify(passwordEncoder, times(1)).encode("plaintext123");
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
        assertEquals("$2a$10$nuevoHash", nuevoUsuario.getPass());
    }

    @Test
    @DisplayName("Almacenar usuario enlaza persona con usuario antes de guardar")
    void usuarioAlmacenar_personaQuedaEnlazadaAlUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setId("USR099");
        nuevoUsuario.setPass("pass123");

        Persona persona = new Persona();
        nuevoUsuario.setPersona(persona);

        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any())).thenReturn(nuevoUsuario);

        usuarioService.usuarioAlmacenar(nuevoUsuario);

        assertEquals(nuevoUsuario, persona.getUsuario());
    }

    // ─── ROLES ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Asignar rol existente a usuario")
    void usuarioRolAlmacenarA_asignaRolCorrectamente() {
        when(usuarioRepository.findById("USR001")).thenReturn(Optional.of(usuarioMock));
        when(rolRepository.findById("ROL002")).thenReturn(Optional.of(rolMock));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        usuarioService.usuarioRolAlmacenarA("USR001", "ROL002");

        verify(usuarioRepository, times(1)).save(usuarioMock);
        assertEquals(rolMock, usuarioMock.getRol());
    }
}
