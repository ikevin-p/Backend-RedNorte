package com.example.Backend_usuarios.controller;

import com.example.Backend_usuarios.model.Rol;
import com.example.Backend_usuarios.security.JwtAuthFilter;
import com.example.Backend_usuarios.service.RolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RolController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — RolController")
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolService rolService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /roles retorna la lista completa de roles")
    void rolListar_retorna200ConLista() throws Exception {
        Rol admin = new Rol();
        admin.setId("ROL001");
        admin.setTag("ADMIN");
        admin.setNombre("Administrador");

        when(rolService.rolListar()).thenReturn(List.of(admin));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tag").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /roles sin roles retorna lista vacia")
    void rolListar_sinRoles_retornaListaVacia() throws Exception {
        when(rolService.rolListar()).thenReturn(List.of());

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /roles crea un nuevo rol y retorna 200")
    void rolAlmacenar_datosValidos_retorna200() throws Exception {
        Rol nuevoRol = new Rol();
        nuevoRol.setId("ROL004");
        nuevoRol.setTag("ENFERMERO");
        nuevoRol.setNombre("Enfermero");

        doNothing().when(rolService).rolAlmacenar(any());

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRol)))
                .andExpect(status().isOk());

        verify(rolService).rolAlmacenar(any(Rol.class));
    }
}
