package com.rednorte.msestablecimientos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.msestablecimientos.model.Establecimiento;
import com.rednorte.msestablecimientos.security.JwtUtilCompartido;
import com.rednorte.msestablecimientos.service.EstablecimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de la capa HTTP de EstablecimientoController.
 */
@WebMvcTest(EstablecimientoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests HTTP — EstablecimientoController")
class EstablecimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EstablecimientoService establecimientoService;

    @MockBean
    private JwtUtilCompartido jwtUtilCompartido;

    private Establecimiento establecimientoEjemplo;

    @BeforeEach
    void setUp() {
        establecimientoEjemplo = new Establecimiento();
        establecimientoEjemplo.setId("EST-001");
        establecimientoEjemplo.setNombre("Hospital Regional del Norte");
        establecimientoEjemplo.setTipo(Establecimiento.TipoEstablecimiento.HOSPITAL);
        establecimientoEjemplo.setComuna("Iquique");
        establecimientoEjemplo.setRegion("Tarapaca");
        establecimientoEjemplo.setEstado(Establecimiento.EstadoEstablecimiento.ACTIVO);
    }

    @Test
    @DisplayName("GET /establecimientos retorna 200 con la lista completa")
    void listarTodos_retorna200ConLista() throws Exception {
        when(establecimientoService.listarTodos()).thenReturn(List.of(establecimientoEjemplo));

        mockMvc.perform(get("/establecimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hospital Regional del Norte"));
    }

    @Test
    @DisplayName("GET /establecimientos/activos retorna solo los activos")
    void listarActivos_retorna200ConActivos() throws Exception {
        when(establecimientoService.listarActivos()).thenReturn(List.of(establecimientoEjemplo));

        mockMvc.perform(get("/establecimientos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /establecimientos/tipo/{tipo} con tipo valido retorna 200")
    void listarPorTipo_tipoValido_retorna200() throws Exception {
        when(establecimientoService.listarPorTipo("HOSPITAL")).thenReturn(List.of(establecimientoEjemplo));

        mockMvc.perform(get("/establecimientos/tipo/HOSPITAL"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /establecimientos/tipo/{tipo} con tipo invalido retorna 400")
    void listarPorTipo_tipoInvalido_retorna400() throws Exception {
        when(establecimientoService.listarPorTipo("INVENTADO"))
                .thenThrow(new IllegalArgumentException("tipo invalido"));

        mockMvc.perform(get("/establecimientos/tipo/INVENTADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /establecimientos/comuna/{comuna} retorna los de esa comuna")
    void listarPorComuna_retorna200() throws Exception {
        when(establecimientoService.listarPorComuna("Iquique")).thenReturn(List.of(establecimientoEjemplo));

        mockMvc.perform(get("/establecimientos/comuna/Iquique"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /establecimientos/region/{region} retorna los de esa region")
    void listarPorRegion_retorna200() throws Exception {
        when(establecimientoService.listarPorRegion("Tarapaca")).thenReturn(List.of(establecimientoEjemplo));

        mockMvc.perform(get("/establecimientos/region/Tarapaca"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /establecimientos/{id} con id existente retorna 200")
    void obtenerPorId_idExistente_retorna200() throws Exception {
        when(establecimientoService.obtenerPorId("EST-001")).thenReturn(establecimientoEjemplo);

        mockMvc.perform(get("/establecimientos/EST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("EST-001"));
    }

    @Test
    @DisplayName("GET /establecimientos/{id} con id inexistente retorna 404")
    void obtenerPorId_idInexistente_retorna404() throws Exception {
        when(establecimientoService.obtenerPorId("EST-999"))
                .thenThrow(new RuntimeException("Establecimiento no encontrado"));

        mockMvc.perform(get("/establecimientos/EST-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /establecimientos crea uno nuevo y retorna 201")
    void crear_datosValidos_retorna201() throws Exception {
        when(establecimientoService.crear(any(Establecimiento.class))).thenReturn(establecimientoEjemplo);

        mockMvc.perform(post("/establecimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(establecimientoEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Hospital Regional del Norte"));
    }

    @Test
    @DisplayName("PUT /establecimientos/{id} con id existente actualiza y retorna 200")
    void actualizar_idExistente_retorna200() throws Exception {
        when(establecimientoService.actualizar(eq("EST-001"), any(Establecimiento.class)))
                .thenReturn(establecimientoEjemplo);

        mockMvc.perform(put("/establecimientos/EST-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(establecimientoEjemplo)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /establecimientos/{id} con id existente retorna 204")
    void eliminar_idExistente_retorna204() throws Exception {
        doNothing().when(establecimientoService).eliminar("EST-001");

        mockMvc.perform(delete("/establecimientos/EST-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /establecimientos/{id} con id inexistente retorna 404")
    void eliminar_idInexistente_retorna404() throws Exception {
        doThrow(new RuntimeException("Establecimiento no encontrado"))
                .when(establecimientoService).eliminar("EST-999");

        mockMvc.perform(delete("/establecimientos/EST-999"))
                .andExpect(status().isNotFound());
    }
}
