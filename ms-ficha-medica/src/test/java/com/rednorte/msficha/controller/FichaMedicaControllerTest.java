package com.rednorte.msficha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.msficha.model.FichaMedica;
import com.rednorte.msficha.security.JwtUtilCompartido;
import com.rednorte.msficha.service.FichaMedicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de FichaMedicaController.
 */
@WebMvcTest(FichaMedicaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — FichaMedicaController")
class FichaMedicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FichaMedicaService fichaMedicaService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private FichaMedica fichaEjemplo;

    @BeforeEach
    void setUp() {
        fichaEjemplo = new FichaMedica();
        fichaEjemplo.setId(1L);
        fichaEjemplo.setUsuarioId("USR010");
        fichaEjemplo.setEstatura(1.75);
        fichaEjemplo.setPeso(70.0);
        fichaEjemplo.setGrupoSanguineo("O+");
        fichaEjemplo.setAlergias("Ninguna conocida");
    }

    @Test
    @DisplayName("GET /ficha/{usuarioId} con ficha existente retorna 200 con los datos")
    void obtener_fichaExistente_retorna200() throws Exception {
        when(fichaMedicaService.obtenerPorUsuario("USR010")).thenReturn(fichaEjemplo);

        mockMvc.perform(get("/ficha/USR010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupoSanguineo").value("O+"))
                .andExpect(jsonPath("$.estatura").value(1.75));
    }

    @Test
    @DisplayName("GET /ficha/{usuarioId} sin ficha previa retorna 204 sin contenido")
    void obtener_sinFichaPrevia_retorna204() throws Exception {
        when(fichaMedicaService.obtenerPorUsuario("USR099")).thenReturn(null);

        mockMvc.perform(get("/ficha/USR099"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /ficha/{usuarioId} crea o actualiza la ficha y retorna 200")
    void guardar_datosValidos_retorna200ConFichaGuardada() throws Exception {
        when(fichaMedicaService.guardar(eq("USR010"), any(FichaMedica.class))).thenReturn(fichaEjemplo);

        mockMvc.perform(put("/ficha/USR010")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fichaEjemplo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value("USR010"));
    }
}
