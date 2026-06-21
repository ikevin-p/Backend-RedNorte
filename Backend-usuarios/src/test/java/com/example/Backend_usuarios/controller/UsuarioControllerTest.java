package com.example.Backend_usuarios.controller;

import com.example.Backend_usuarios.model.Persona;
import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.model.Usuario;
import com.example.Backend_usuarios.security.JwtAuthFilter;
import com.example.Backend_usuarios.security.JwtUtil;
import com.example.Backend_usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de UsuarioController.
 *
 * Se mockea JwtAuthFilter (filtro real @Component que Spring escanea
 * incluso con los filtros desactivados via addFilters=false) y JwtUtil
 * (usado directamente dentro del metodo login() para generar el token,
 * a diferencia del resto de microservicios donde solo se usa dentro
 * del filtro).
 */
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private Usuario usuarioEjemplo;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setId("ROL002");
        rol.setTag("PACIENTE");
        rol.setNombre("Paciente");

        Persona persona = new Persona();
        persona.setId("PER010");
        persona.setApellido1("Perez");
        persona.setApellido2("Castro");
        persona.setRut("15678901-2");

        usuarioEjemplo = new Usuario();
        usuarioEjemplo.setId("USR010");
        usuarioEjemplo.setMail("juan.perez@correo.cl");
        usuarioEjemplo.setEstado("ACTIVO");
        usuarioEjemplo.setRol(rol);
        usuarioEjemplo.setPersona(persona);
    }

    @Test
    @DisplayName("GET /usuarios retorna la lista completa de usuarios como DTO")
    void usuarioListar_retorna200ConLista() throws Exception {
        when(usuarioService.usuarioListar()).thenReturn(List.of(usuarioEjemplo));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mail").value("juan.perez@correo.cl"))
                .andExpect(jsonPath("$[0].rol.tag").value("PACIENTE"))
                .andExpect(jsonPath("$[0].persona.apellido1").value("Perez"));
    }

    @Test
    @DisplayName("GET /usuarios sin usuarios retorna lista vacia")
    void usuarioListar_sinUsuarios_retornaListaVacia() throws Exception {
        when(usuarioService.usuarioListar()).thenReturn(List.of());

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /usuarios crea un usuario nuevo y retorna 200")
    void usuarioAlmacenar_datosValidos_retorna200() throws Exception {
        doNothing().when(usuarioService).usuarioAlmacenar(any(Usuario.class));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioEjemplo)))
                .andExpect(status().isOk());

        verify(usuarioService).usuarioAlmacenar(any(Usuario.class));
    }

    @Test
    @DisplayName("POST /usuarios/login con credenciales correctas retorna 200 con token")
    void login_credencialesCorrectas_retorna200ConToken() throws Exception {
        when(usuarioService.login("juan.perez@correo.cl", "paciente123")).thenReturn(usuarioEjemplo);
        when(jwtUtil.generarToken("juan.perez@correo.cl", "PACIENTE", "USR010")).thenReturn("jwt-token-falso");

        Map<String, String> credenciales = Map.of("mail", "juan.perez@correo.cl", "pass", "paciente123");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-falso"))
                .andExpect(jsonPath("$.mail").value("juan.perez@correo.cl"));
    }

    @Test
    @DisplayName("POST /usuarios/login con credenciales incorrectas retorna 401")
    void login_credencialesIncorrectas_retorna401() throws Exception {
        when(usuarioService.login("juan.perez@correo.cl", "incorrecta")).thenReturn(null);

        Map<String, String> credenciales = Map.of("mail", "juan.perez@correo.cl", "pass", "incorrecta");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil, never()).generarToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /usuarios/login usa PACIENTE como rol por defecto si el usuario no tiene rol asignado")
    void login_usuarioSinRol_usaRolPacientePorDefecto() throws Exception {
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setId("USR099");
        usuarioSinRol.setMail("sinrol@correo.cl");
        usuarioSinRol.setRol(null);

        when(usuarioService.login("sinrol@correo.cl", "clave123")).thenReturn(usuarioSinRol);
        when(jwtUtil.generarToken("sinrol@correo.cl", "PACIENTE", "USR099")).thenReturn("jwt-token-default");

        Map<String, String> credenciales = Map.of("mail", "sinrol@correo.cl", "pass", "clave123");

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-default"));

        verify(jwtUtil).generarToken("sinrol@correo.cl", "PACIENTE", "USR099");
    }
}
